package com.jinjinjara.pola.rag.service;

import com.jinjinjara.pola.data.service.FileTagService;
import com.jinjinjara.pola.rag.dto.common.QueryPreprocessResult;
import com.jinjinjara.pola.rag.dto.common.RagSearchSource;
import com.jinjinjara.pola.rag.dto.common.SearchRow;
import com.jinjinjara.pola.rag.dto.response.RagSearchResponse;
import com.jinjinjara.pola.rag.util.QueryPreprocessor;
import com.jinjinjara.pola.rag.util.RagPostProcessor;
import com.jinjinjara.pola.rag.util.RagProperties;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagSearchService {

    private final EmbeddingSearchService embeddingSearchService;
    private final S3Service s3Service;
    private final QueryPreprocessor queryPreprocessor;
    private final RagPostProcessor ragPostProcessor;
    private final FileTagService fileTagService;
    private final RagProperties ragProperties;

    public RagSearchResponse search(Users user, String query, int limit) {
        Long userId = user.getId();
        log.info("[RagSearch] userId={}, query='{}', limit={}", userId, query, limit);

        // 1) 전처리 + 타입 라우팅
        QueryPreprocessResult pre = queryPreprocessor.preprocess(query);
        String cleaned = pre.getCleanedQuery();
        LocalDate start = pre.getStartDate();
        LocalDate end = pre.getEndDate();
        var type = pre.getQueryType();

        // 2) 검색
        List<SearchRow> rows = embeddingSearchService.searchSimilarFiles(userId, cleaned, limit, start, end);

        log.info("[RagSearch] RAW SEARCH RESULTS (count={})", rows.size());
        for (int i = 0; i < rows.size(); i++) {
            SearchRow r = rows.get(i);
            log.info("  [{}] id={} score={} context={}",
                    i,
                    r.getId(),
                    r.getRelevanceScore(),
                    r.getContext());
        }

        if (rows.isEmpty()) {
            return new RagSearchResponse("검색 결과가 없습니다.", List.of());
        }

        // 3) 태그 병합
        List<RagSearchSource> sources = rows.stream()
                .map(r -> {
                    // 태그 조회
                    List<String> tagNames = fileTagService.getTagsByFile(r.getId(),user).stream()
                            .map(com.jinjinjara.pola.data.dto.response.TagResponse::getTagName)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .toList();

                    return RagSearchSource.builder()
                            .id(r.getId())
                            .src(s3Service.generatePreviewUrl(r.getSrc(),r.getType()))
                            .type(r.getType())
                            .context(r.getContext())
                            .favorite(r.getFavorite())
                            .ocrText(r.getOcrText())
                            .createdAt(
                                    r.getCreatedAt() == null
                                            ? null
                                            : r.getCreatedAt().toLocalDateTime()
                            )
                            .relevanceScore(r.getRelevanceScore())
                            .tags(tagNames)
                            .build();
                })
                .toList();

        // 4) perType 정책 결정 (perType → 전역)
        var sim = ragProperties.getSimilarity();
        var tp  = sim.getPerType().get(type); // 없으면 전역 사용
        double minSim    = (tp != null && tp.getMin() != null) ? tp.getMin() : sim.getMin();
        double keepRatio = (tp != null && tp.getKeepRatio() != null) ? tp.getKeepRatio() : sim.getKeepRatio();

        List<Double> backoff = (tp != null && tp.getBackoff() != null && !tp.getBackoff().isEmpty())
                ? tp.getBackoff()
                : (sim.getBackoff() != null ? sim.getBackoff() : Collections.emptyList());

        // 5) Step A: top1 유효성 검사 (min + backoff)
        double top1 = sources.get(0).getRelevanceScore() == null
                ? 0.0
                : sources.get(0).getRelevanceScore();

        // top1이 0 이하고, minSim이 양수면 그냥 "관련 없음"으로 처리
        if (top1 <= 0.0 && minSim > 0.0) {
            log.debug("[RagSearch] type={}, top1={}, min={} → top1<=0, no result", type, top1, minSim);
            return new RagSearchResponse("관련도가 낮아 결과가 없습니다.", List.of());
        }

        // factors: 1.0(기본) + backoff 계수들
        List<Double> factors = new ArrayList<>();
        factors.add(1.0);
        factors.addAll(backoff);

        Double appliedFactor = null;
        double effectiveMin = 0.0;

        for (double f : factors) {
            double threshold = minSim * f;
            if (top1 >= threshold) {
                appliedFactor = f;
                effectiveMin = threshold;
                break;
            }
        }

        // 어떤 단계에서도 통과 못 하면 → 검색 결과 없음
        if (appliedFactor == null) {
            log.debug("[RagSearch] type={}, top1={}, min={}, backoff={} → no factor passed",
                    type, top1, minSim, backoff);
            return new RagSearchResponse("관련도가 낮아 결과가 없습니다.", List.of());
        }

        // 6) Step B: 나머지 문서 필터링 기준 계산
        double ratioCut = top1 * keepRatio;
        double finalCut = Math.max(effectiveMin, ratioCut);

        log.info("[RagSearch] type={}, top1={}, min={}, keep={}, appliedFactor={}, " +
                        "effectiveMin={}, ratioCut={}, finalCut={}",
                type, top1, minSim, keepRatio, appliedFactor, effectiveMin, ratioCut, finalCut);

        // finalCut 이상인 문서만 사용
        List<RagSearchSource> filtered = sources.stream()
                .filter(s -> s.getRelevanceScore() != null
                        && s.getRelevanceScore() >= finalCut)
                .toList();

        log.info("[RagSearch] FILTERED RESULTS (count={})", filtered.size());
        for (int i = 0; i < filtered.size(); i++) {
            var f = filtered.get(i);
            log.info("  [{}] id={} score={} context={}",
                    i,
                    f.getId(),
                    f.getRelevanceScore(),
                    f.getContext());
        }

        if (filtered.isEmpty()) {
            log.debug("[RagSearch] filtered is empty after finalCut={}, type={}", finalCut, type);
            return new RagSearchResponse("관련도가 낮아 결과가 없습니다.", List.of());
        }

        // 7) LLM 후처리
        String answer = ragPostProcessor.generateAnswer(query, filtered);
        return new RagSearchResponse(answer, filtered);
    }
}

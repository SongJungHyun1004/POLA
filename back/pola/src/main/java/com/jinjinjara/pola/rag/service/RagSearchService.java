package com.jinjinjara.pola.rag.service;

import com.jinjinjara.pola.data.repository.FileTagRepository;
import com.jinjinjara.pola.data.service.FileTagService;
import com.jinjinjara.pola.rag.dto.common.QueryPreprocessResult;
import com.jinjinjara.pola.rag.dto.common.RagSearchSource;
import com.jinjinjara.pola.rag.dto.common.SearchRow;
import com.jinjinjara.pola.rag.dto.common.TagRow;
import com.jinjinjara.pola.rag.dto.response.RagSearchResponse;
import com.jinjinjara.pola.rag.util.QueryPreprocessor;
import com.jinjinjara.pola.rag.util.RagPostProcessor;
import com.jinjinjara.pola.rag.util.RagProperties;
import com.jinjinjara.pola.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDate;
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

    public RagSearchResponse search(Long userId, String query, int limit) {
        log.info("[RagSearch] userId={}, query='{}', limit={}", userId, query, limit);

        // 1) 전처리 + 타입 라우팅
        QueryPreprocessResult pre = queryPreprocessor.preprocess(query);
        String cleaned = pre.getCleanedQuery();
        LocalDate start = pre.getStartDate();
        LocalDate end = pre.getEndDate();
        var type = pre.getQueryType();

        // 2) 검색
        List<SearchRow> rows = embeddingSearchService.searchSimilarFiles(userId, cleaned, limit, start, end);
        if (rows.isEmpty()) return new RagSearchResponse("검색 결과가 없습니다.", List.of());

        // 3) 태그 병합
        List<RagSearchSource> sources = rows.stream()
                .map(r -> {
                    List<String> tagNames = fileTagService.getTagsByFile(r.getId()).stream()
                            .map(com.jinjinjara.pola.data.dto.response.TagResponse::getTagName)
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .distinct()
                            .toList();
                    return RagSearchSource.builder()
                            .id(r.getId())
                            .src(s3Service.generateDownloadUrl(r.getSrc()))
                            .context(r.getContext())
                            .relevanceScore(r.getRelevanceScore())
                            .tags(tagNames)
                            .build();
                })
                .toList();

        // 4) perType 정책 결정
        var sim = ragProperties.getSimilarity();
        var tp  = sim.getPerType().get(type); // 없으면 전역으로
        double minSim    = (tp != null && tp.getMin() != null) ? tp.getMin() : sim.getMin();
        double keepRatio = (tp != null && tp.getKeepRatio() != null) ? tp.getKeepRatio() : sim.getKeepRatio();
        List<Double> backoff = (tp != null && tp.getBackoff() != null && !tp.getBackoff().isEmpty())
                ? tp.getBackoff()
                : List.of(0.25, 0.10, 0.0); // 기본 단계

        double top1  = sources.get(0).getRelevanceScore() == null ? 0.0 : sources.get(0).getRelevanceScore();
        double relCut = Math.max(minSim, top1 * keepRatio);
        log.debug("[RagSearch] type={}, top1={}, min={}, keep={}, relCut={}", type, top1, minSim, keepRatio, relCut);

        // 5) 1차 컷
        List<RagSearchSource> filtered = sources.stream()
                .filter(s -> s.getRelevanceScore() != null && s.getRelevanceScore() >= relCut)
                .toList();

        // 6) 결과 0개면 backoff 단계적으로 완화
        if (filtered.isEmpty()) {
            // 6-1) 비율 완화: top1 * b
            for (double b : backoff) {
                double cut = Math.max(minSim, top1 * b);
                filtered = sources.stream()
                        .filter(s -> s.getRelevanceScore() != null && s.getRelevanceScore() >= cut)
                        .toList();
                log.debug("[RagSearch] backoff(ratio)={} → cut={}, remained={}", b, cut, filtered.size());
                if (!filtered.isEmpty()) break;
            }

            // 6-2) 절대 바닥 완화: floor 로 직접 설정 (b가 minSim보다 작은 항목만 사용)
            if (filtered.isEmpty()) {
                for (double floor : backoff) {
                    if (floor >= minSim) continue;
                    double cut = floor;
                    filtered = sources.stream()
                            .filter(s -> s.getRelevanceScore() != null && s.getRelevanceScore() >= cut)
                            .toList();
                    log.debug("[RagSearch] backoff(abs)={} → cut={}, remained={}", floor, cut, filtered.size());
                    if (!filtered.isEmpty()) break;
                }
            }
        }

        if (filtered.isEmpty()) {
            return new RagSearchResponse("관련도가 낮아 결과가 없습니다.", List.of());
        }

        // 7) LLM 후처리
        String answer = ragPostProcessor.generateAnswer(query, filtered);
        return new RagSearchResponse(answer, filtered);
    }
}
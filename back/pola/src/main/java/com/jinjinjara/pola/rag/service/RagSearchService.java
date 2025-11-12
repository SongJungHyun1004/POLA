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

    @Value("${rag.similarity.min}")
    private double minSim;

    @Value("${rag.similarity.keepRatio}")
    private double keepRatio;

    public RagSearchResponse search(Long userId, String query, int limit) {
        log.info("[RagSearch] userId={}, query='{}', limit={}", userId, query, limit);

        QueryPreprocessResult pre = queryPreprocessor.preprocess(query);
        String cleaned = pre.getCleanedQuery();
        LocalDate start = pre.getStartDate();
        LocalDate end = pre.getEndDate();

        List<SearchRow> rows = embeddingSearchService.searchSimilarFiles(userId, cleaned, limit, start, end);
        if (rows.isEmpty()) {
            return new RagSearchResponse("검색 결과가 없습니다.", List.of());
        }

        // 1) rows -> sources 매핑
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

        // 2) 유사도 컷 계산 (절대/상대)
        double top1 = sources.get(0).getRelevanceScore() == null ? 0.0 : sources.get(0).getRelevanceScore();
        double relCut = Math.max(minSim, top1 * keepRatio);

        // 3) 컷 적용
        List<RagSearchSource> filtered = sources.stream()
                .filter(s -> s.getRelevanceScore() != null && s.getRelevanceScore() >= relCut)
                .toList();

        // 4) 0개면 LLM 호출 스킵
        if (filtered.isEmpty()) {
            return new RagSearchResponse("관련도가 낮아 결과가 없습니다.", List.of());
        }

        // 5) LLM 후처리
        String answer = ragPostProcessor.generateAnswer(query, filtered);
        return new RagSearchResponse(answer, filtered);
    }
}
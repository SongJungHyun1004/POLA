package com.jinjinjara.pola.rag.service;

import com.jinjinjara.pola.rag.dto.common.QueryPreprocessResult;
import com.jinjinjara.pola.rag.dto.common.RagSearchSource;
import com.jinjinjara.pola.rag.dto.common.SearchRow;
import com.jinjinjara.pola.rag.dto.response.RagSearchResponse;
import com.jinjinjara.pola.rag.util.QueryPreprocessor;
import com.jinjinjara.pola.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagSearchService {

    private final EmbeddingSearchService embeddingSearchService;
    private final S3Service s3Service;
    private final QueryPreprocessor queryPreprocessor;

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

        List<RagSearchSource> sources = rows.stream()
                .map(r -> new RagSearchSource(
                        r.getId(),
                        s3Service.generateDownloadUrl(r.getSrc()),
                        r.getContext(),
                        r.getRelevanceScore()
                ))
                .toList();

        String answer = String.format("%d건의 관련 파일을 찾았습니다.", sources.size());
        return new RagSearchResponse(answer, sources);
    }
}
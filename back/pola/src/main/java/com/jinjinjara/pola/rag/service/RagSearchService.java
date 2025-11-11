package com.jinjinjara.pola.rag.service;

import com.jinjinjara.pola.rag.dto.common.RagSearchSource;
import com.jinjinjara.pola.rag.dto.common.SearchRow;
import com.jinjinjara.pola.rag.dto.response.RagSearchResponse;
import com.jinjinjara.pola.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class RagSearchService {

    private final EmbeddingSearchService embeddingSearchService;
    private final S3Service s3Service;

    public RagSearchResponse search(Long userId, String query, int limit) {
        log.info("[RagSearch] userId={}, query='{}', limit={}", userId, query, limit);

        List<SearchRow> rows = embeddingSearchService.searchSimilarFiles(userId, query, limit);
        if (rows.isEmpty()) {
            return new RagSearchResponse("검색 결과가 없습니다.", List.of());
        }

        List<RagSearchSource> sources = rows.stream()
                .map(r -> new RagSearchSource(
                        r.getId(),                                   // 파일 ID (쿼리에서 f.id AS id)
                        s3Service.generateDownloadUrl(r.getSrc()),   // S3 프리사인드 URL
                        r.getContext(),                              // 컨텍스트
                        r.getRelevanceScore()                        // 유사도 스코어
                ))
                .toList();

        String answer = String.format("%d건의 관련 파일을 찾았습니다.", sources.size());
        return new RagSearchResponse(answer, sources);
    }
}
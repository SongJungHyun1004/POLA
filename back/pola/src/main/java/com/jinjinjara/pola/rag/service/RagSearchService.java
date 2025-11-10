package com.jinjinjara.pola.rag.service;

import com.jinjinjara.pola.rag.dto.common.RagSearchSource;
import com.jinjinjara.pola.rag.dto.response.RagSearchResponse;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.vision.entity.FileEmbeddings;
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

        List<FileEmbeddings> hits = embeddingSearchService.searchSimilarFiles(userId, query, limit);
        if (hits.isEmpty()) {
            return new RagSearchResponse("검색 결과가 없습니다.", List.of());
        }

        List<RagSearchSource> sources = hits.stream()
                .map(e -> new RagSearchSource(
                        e.getFile().getId(),                   // file id
                        s3Service.generateDownloadUrl(e.getFile().getSrc()),        // S3 다운 링크
                        e.getContext(),              // 요약/설명 텍스트
                        null                         // relevanceScore(선택)
                ))
                .toList();

        String answer = String.format("%d건의 관련 파일을 찾았습니다.", sources.size());
        return new RagSearchResponse(answer, sources);
    }
}
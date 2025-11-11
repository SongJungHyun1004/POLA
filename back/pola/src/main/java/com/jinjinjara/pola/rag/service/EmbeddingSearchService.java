package com.jinjinjara.pola.rag.service;

import com.jinjinjara.pola.vision.entity.FileEmbeddings;
import com.jinjinjara.pola.vision.repository.FileEmbeddingsRepository;
import com.jinjinjara.pola.vision.service.EmbeddingService;
import com.pgvector.PGvector;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingSearchService {

    private final EmbeddingService embeddingService;
    private final FileEmbeddingsRepository fileEmbeddingsRepository;

    public List<FileEmbeddings> searchSimilarFiles(Long userId, String query, int limit) {
        log.info("[EmbeddingSearch] userId={}, query='{}', limit={}", userId, query, limit);

        float[] q = embeddingService.embedQuery(query);
        if (q == null || q.length == 0) {
            log.warn("[EmbeddingSearch] empty embedding for '{}'", query);
            return List.of();
        }

        String vec = toVectorLiteral(q);

        List<FileEmbeddings> results = fileEmbeddingsRepository.findSimilarFiles(userId, vec, limit);
        log.info("[EmbeddingSearch] found {} results", results.size());
        return results;
    }

    private static String toVectorLiteral(float[] v) {
        StringBuilder sb = new StringBuilder(v.length * 8);
        sb.append('[');
        for (int i = 0; i < v.length; i++) {
            if (i > 0) sb.append(',');

            sb.append(Float.toString(v[i]));
        }
        sb.append(']');
        return sb.toString();
    }
}

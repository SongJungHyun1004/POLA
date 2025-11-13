package com.jinjinjara.pola.rag.service;


import com.jinjinjara.pola.rag.dto.common.SearchRow;
import com.jinjinjara.pola.vision.repository.FileEmbeddingsRepository;
import com.jinjinjara.pola.vision.service.EmbeddingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmbeddingSearchService {

    private final EmbeddingService embeddingService;
    private final FileEmbeddingsRepository fileEmbeddingsRepository;

    public List<SearchRow> searchSimilarFiles(
            Long userId, String query, int limit,
            LocalDate startDate, LocalDate endDate
    ) {
        log.info("[EmbeddingSearch] userId={}, query='{}', range={}~{}, limit={}",
                userId, query, startDate, endDate, limit);

        float[] q = embeddingService.embedQuery(query);
        if (q == null || q.length == 0) {
            log.warn("[EmbeddingSearch] empty embedding for '{}'", query);
            return List.of();
        }

        String vec = toVectorLiteral(q);

        if (startDate != null && endDate != null) {
            LocalDateTime startTs = startDate.atStartOfDay();
            LocalDateTime endTs = endDate.plusDays(1).atStartOfDay().minusNanos(1); // inclusive day end

            return fileEmbeddingsRepository.findSimilarFilesWithScoreAndDate(
                    userId, vec, limit, startTs, endTs
            );
        } else {
            return fileEmbeddingsRepository.findSimilarFilesWithScore(userId, vec, limit);
        }
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

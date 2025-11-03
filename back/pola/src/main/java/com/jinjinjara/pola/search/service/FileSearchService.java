package com.jinjinjara.pola.search.service;

import com.jinjinjara.pola.search.document.FileDocument;
import lombok.RequiredArgsConstructor;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public List<FileDocument> search(String keyword) {
        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .multiMatch(mm -> mm
                                .query(keyword)
                                .fields("categoryName", "tags", "context", "ocrText")
                        )
                )
                .build();

        SearchHits<FileDocument> hits =
                elasticsearchOperations.search(query, FileDocument.class);

        return hits.getSearchHits().stream()
                .map(hit -> hit.getContent())
                .collect(Collectors.toList());
    }
}

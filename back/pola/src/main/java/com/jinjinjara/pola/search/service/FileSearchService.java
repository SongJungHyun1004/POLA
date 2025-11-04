package com.jinjinjara.pola.opensearch.service;


import com.jinjinjara.pola.search.model.FileSearch;
import lombok.RequiredArgsConstructor;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch.core.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FileSearchService {

    private final OpenSearchClient client;
    private static final String INDEX_NAME = "files";

    /** 생성/갱신 */
    public void save(FileSearch file) throws IOException {
        client.index(i -> i.index(INDEX_NAME)
                .id(String.valueOf(file.getFileId()))
                .document(file));
    }

    /** 단건 조회 */
    public FileSearch get(Long id) throws IOException {
        GetResponse<FileSearch> res = client.get(g -> g
                .index(INDEX_NAME).id(String.valueOf(id)), FileSearch.class);
        return res.found() ? res.source() : null;
    }

    /** 삭제 */
    public void delete(Long id) throws IOException {
        client.delete(d -> d.index(INDEX_NAME).id(String.valueOf(id)));
    }

    /** userId로 검색 (정확 매칭) */
    public List<FileSearch> searchByUserId(Long userId) throws IOException {
        SearchResponse<FileSearch> res = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.term(t -> t
                        .field("userId")
                        .value(FieldValue.of(userId))
                )), FileSearch.class);

        return res.hits().hits().stream().map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    /** category 텍스트 검색 (match) */
    public List<FileSearch> searchByCategory(String category) throws IOException {
        SearchResponse<FileSearch> res = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.match(m -> m
                        .field("category")
                        .query(FieldValue.of(category))
                )), FileSearch.class);

        return res.hits().hits().stream().map(hit -> hit.source())
                .collect(Collectors.toList());
    }
}

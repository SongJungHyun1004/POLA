package com.jinjinjara.pola.search.service;


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

    // ========== 새로운 검색 메서드 ==========

    /**
     * 태그 기반 검색 (단일 태그)
     * @param userId 사용자 ID
     * @param tag 검색할 태그
     */
    public List<FileSearch> searchByTag(Long userId, String tag) throws IOException {
        SearchResponse<FileSearch> res = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("userId").value(FieldValue.of(userId))))
                        .must(m -> m.match(match -> match.field("tags").query(FieldValue.of(tag))))
                )), FileSearch.class);

        return res.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    /**
     * 카테고리별 검색 (사용자 필터링 포함)
     * @param userId 사용자 ID
     * @param categoryName 카테고리명
     */
    public List<FileSearch> searchByCategoryName(Long userId, String categoryName) throws IOException {
        SearchResponse<FileSearch> res = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("userId").value(FieldValue.of(userId))))
                        .must(m -> m.term(t -> t.field("categoryName").value(FieldValue.of(categoryName))))
                )), FileSearch.class);

        return res.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    /**
     * OCR 텍스트 검색
     * @param userId 사용자 ID
     * @param keyword 검색할 키워드
     */
    public List<FileSearch> searchByOcrText(Long userId, String keyword) throws IOException {
        SearchResponse<FileSearch> res = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("userId").value(FieldValue.of(userId))))
                        .must(m -> m.match(match -> match.field("ocrText").query(FieldValue.of(keyword))))
                )), FileSearch.class);

        return res.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    /**
     * Context(설명) 검색
     * @param userId 사용자 ID
     * @param keyword 검색할 키워드
     */
    public List<FileSearch> searchByContext(Long userId, String keyword) throws IOException {
        SearchResponse<FileSearch> res = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("userId").value(FieldValue.of(userId))))
                        .must(m -> m.match(match -> match.field("context").query(FieldValue.of(keyword))))
                )), FileSearch.class);

        return res.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }

    /**
     * 통합 검색 (태그 + OCR + Context)
     * @param userId 사용자 ID
     * @param keyword 검색할 키워드
     */
    public List<FileSearch> searchAll(Long userId, String keyword) throws IOException {
        SearchResponse<FileSearch> res = client.search(s -> s
                .index(INDEX_NAME)
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("userId").value(FieldValue.of(userId))))
                        .should(sh -> sh.match(match -> match.field("tags").query(FieldValue.of(keyword))))
                        .should(sh -> sh.match(match -> match.field("ocrText").query(FieldValue.of(keyword))))
                        .should(sh -> sh.match(match -> match.field("context").query(FieldValue.of(keyword))))
                        .minimumShouldMatch(String.valueOf(1))
                )), FileSearch.class);

        return res.hits().hits().stream()
                .map(hit -> hit.source())
                .collect(Collectors.toList());
    }
}

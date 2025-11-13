package com.jinjinjara.pola.search.service;


import com.jinjinjara.pola.search.model.FileSearch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.opensearch.client.opensearch.OpenSearchClient;
import org.opensearch.client.opensearch._types.FieldValue;
import org.opensearch.client.opensearch._types.query_dsl.BoolQuery;
import org.opensearch.client.opensearch.core.*;
import org.opensearch.client.opensearch.indices.AnalyzeRequest;
import org.opensearch.client.opensearch.indices.AnalyzeResponse;
import org.opensearch.client.opensearch.indices.analyze.AnalyzeToken;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
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

    // ========== Nori 토큰 분석 ==========

    /**
     * Nori 분석기로 검색어를 토큰으로 분해
     *
     * 예: "강아지잠옷" → ["강아지", "잠옷"]
     * 예: "립스틱" → ["립스틱"]
     * 예: "립" → ["립"]
     *
     * @param text 분석할 텍스트
     * @return 토큰 목록
     */
    private List<String> analyzeWithNori(String text) throws IOException {
        try {
            AnalyzeRequest request = AnalyzeRequest.of(a -> a
                    .index(INDEX_NAME)
                    .analyzer("nori_analyzer")
                    .text(text)
            );

            AnalyzeResponse response = client.indices().analyze(request);

            List<String> tokens = response.tokens().stream()
                    .map(AnalyzeToken::token)
                    .collect(Collectors.toList());

            log.debug("Nori 분석: '{}' → {}", text, tokens);
            return tokens;

        } catch (Exception e) {
            // 분석 실패 시 원본 텍스트를 단일 토큰으로 반환
            log.warn("Nori 분석 실패: '{}', 원본 사용", text, e);
            return Collections.singletonList(text);
        }
    }

    // ========== 새로운 검색 메서드 ==========

    /**
     * 태그 기반 검색 (단일 태그)
     * 한 글자 검색 및 부분 검색을 지원합니다.
     *
     * 검색 전략:
     * 1. Nori로 검색어 토큰 분석
     * 2. 토큰이 2개 이상 → must(AND) 조건 (과다 매칭 방지)
     * 3. 토큰이 1개 → should(OR) 조건 + Edge N-gram (자동완성)
     *
     * 예시:
     * - "강아지잠옷" → ["강아지", "잠옷"] → must("강아지") AND must("잠옷")
     * - "립" → ["립"] → should("립") OR should(edge_ngram)
     *
     * @param userId 사용자 ID
     * @param tag 검색할 태그
     */
    public List<FileSearch> searchByTag(Long userId, String tag) throws IOException {
        // 1. Nori로 토큰 분석
        List<String> tokens = analyzeWithNori(tag);

        // 2. 토큰 수에 따라 쿼리 전략 분기
        if (tokens.size() >= 2) {
            // 다중 토큰: must(AND) 조건 - 모든 토큰이 포함되어야 함
            log.debug("다중 토큰 검색 (AND): {}", tokens);

            SearchResponse<FileSearch> res = client.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> {
                        BoolQuery.Builder bool = new BoolQuery.Builder();

                        // userId 필터
                        bool.must(m -> m.term(t -> t.field("userId").value(FieldValue.of(userId))));

                        // 각 토큰을 must 조건으로 추가 (AND)
                        for (String token : tokens) {
                            bool.must(m -> m.match(match -> match.field("tags").query(FieldValue.of(token))));
                        }

                        return q.bool(bool.build());
                    }), FileSearch.class);

            return res.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());

        } else {
            // 단일 토큰: should(OR) 조건 - Edge N-gram과 조합
            log.debug("단일 토큰 검색 (OR + Edge N-gram): {}", tokens);

            SearchResponse<FileSearch> res = client.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q.bool(b -> b
                            .must(m -> m.term(t -> t.field("userId").value(FieldValue.of(userId))))
                            // Nori 형태소 분석 매칭
                            .should(sh -> sh.match(match -> match.field("tags").query(FieldValue.of(tag))))
                            // Edge N-gram 매칭 (한 글자 및 접두사 검색)
                            .should(sh -> sh.match(match -> match.field("tags.edge_ngram").query(FieldValue.of(tag))))
                            // Wildcard 매칭 (fallback)
                            .should(sh -> sh.wildcard(w -> w
                                    .field("tags.keyword")
                                    .value("*" + tag + "*")
                                    .caseInsensitive(true)
                            ))
                            .minimumShouldMatch(String.valueOf(1))
                    )), FileSearch.class);

            return res.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());
        }
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
     * 한 글자 검색 및 부분 검색을 지원합니다.
     *
     * 검색 전략:
     * 1. Nori로 검색어 토큰 분석
     * 2. 토큰이 2개 이상 → must(AND) 조건 (과다 매칭 방지)
     * 3. 토큰이 1개 → should(OR) 조건 + Edge N-gram (자동완성)
     * 4. 텍스트 파일의 풀텍스트는 ocrText에 저장되므로 반드시 ocrText 검색 포함
     *
     * @param userId 사용자 ID
     * @param keyword 검색할 키워드
     */
    public List<FileSearch> searchAll(Long userId, String keyword) throws IOException {
        // 1. Nori로 토큰 분석
        List<String> tokens = analyzeWithNori(keyword);

        // 2. 토큰 수에 따라 쿼리 전략 분기
        if (tokens.size() >= 2) {
            // 다중 토큰: must(AND) 조건
            log.debug("통합 검색 - 다중 토큰 (AND): {}", tokens);

            SearchResponse<FileSearch> res = client.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> {
                        BoolQuery.Builder bool = new BoolQuery.Builder();

                        // userId 필터
                        bool.must(m -> m.term(t -> t.field("userId").value(FieldValue.of(userId))));

                        // 각 토큰이 tags, ocrText, context 중 하나에는 반드시 존재해야 함
                        for (String token : tokens) {
                            bool.must(m -> m.bool(b -> b
                                    .should(sh -> sh.match(match -> match.field("tags").query(FieldValue.of(token))))
                                    .should(sh -> sh.match(match -> match.field("ocrText").query(FieldValue.of(token))))
                                    .should(sh -> sh.match(match -> match.field("context").query(FieldValue.of(token))))
                                    .minimumShouldMatch("1")
                            ));
                        }

                        return q.bool(bool.build());
                    }), FileSearch.class);

            return res.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());

        } else {
            // 단일 토큰: should(OR) 조건 + Edge N-gram
            log.debug("통합 검색 - 단일 토큰 (OR + Edge N-gram): {}", tokens);

            SearchResponse<FileSearch> res = client.search(s -> s
                    .index(INDEX_NAME)
                    .query(q -> q.bool(b -> b
                            .must(m -> m.term(t -> t.field("userId").value(FieldValue.of(userId))))
                            // Nori 형태소 분석 매칭
                            .should(sh -> sh.match(match -> match.field("tags").query(FieldValue.of(keyword))))
                            .should(sh -> sh.match(match -> match.field("ocrText").query(FieldValue.of(keyword))))
                            .should(sh -> sh.match(match -> match.field("context").query(FieldValue.of(keyword))))
                            // Edge N-gram 매칭 (한 글자 및 접두사 검색)
                            .should(sh -> sh.match(match -> match.field("tags.edge_ngram").query(FieldValue.of(keyword))))
                            .should(sh -> sh.match(match -> match.field("ocrText.edge_ngram").query(FieldValue.of(keyword))))
                            .should(sh -> sh.match(match -> match.field("context.edge_ngram").query(FieldValue.of(keyword))))
                            // Wildcard 매칭 (fallback)
                            .should(sh -> sh.wildcard(w -> w
                                    .field("tags.keyword")
                                    .value("*" + keyword + "*")
                                    .caseInsensitive(true)
                            ))
                            .minimumShouldMatch(String.valueOf(1))
                    )), FileSearch.class);

            return res.hits().hits().stream()
                    .map(hit -> hit.source())
                    .collect(Collectors.toList());
        }
    }

    /**
     * 태그 자동완성 검색
     * 한 글자 검색 및 접두사 검색을 지원합니다.
     *
     * Edge N-gram 사용:
     * - 접두사 매칭에 최적화 (자동완성 UI에 적합)
     * - 인덱스 크기 최소화 (일반 N-gram 대비)
     * - "립"으로 검색 시 "립", "립스틱", "립밤" 등 접두사로 시작하는 태그 매칭
     *
     * @param userId 사용자 ID
     * @param keyword 검색할 키워드 (예: "리", "립", "react")
     * @return 매칭되는 고유한 태그 목록
     */
    public List<String> searchTagSuggestions(Long userId, String keyword) throws IOException {
        // Edge N-gram + wildcard 조합으로 검색
        SearchResponse<FileSearch> res = client.search(s -> s
                .index(INDEX_NAME)
                .size(100) // 충분한 결과 가져오기
                .query(q -> q.bool(b -> b
                        .must(m -> m.term(t -> t.field("userId").value(FieldValue.of(userId))))
                        // Edge N-gram: 접두사 검색
                        .should(sh -> sh.match(match -> match.field("tags.edge_ngram").query(FieldValue.of(keyword))))
                        // Wildcard: fallback (중간 매칭)
                        .should(sh -> sh.wildcard(w -> w
                                .field("tags.keyword")
                                .value("*" + keyword + "*")
                                .caseInsensitive(true)
                        ))
                        .minimumShouldMatch(String.valueOf(1))
                )), FileSearch.class);

        // 모든 파일의 tags 필드를 파싱하여 개별 태그로 분리
        Set<String> uniqueTags = new HashSet<>();

        res.hits().hits().stream()
                .map(hit -> hit.source())
                .filter(file -> file.getTags() != null && !file.getTags().isEmpty())
                .forEach(file -> {
                    // 쉼표로 구분된 태그를 분리
                    String[] tags = file.getTags().split(",");
                    for (String tag : tags) {
                        String trimmedTag = tag.trim();
                        // 키워드를 포함하는 태그만 추가 (대소문자 무시)
                        if (trimmedTag.toLowerCase().contains(keyword.toLowerCase())) {
                            uniqueTags.add(trimmedTag);
                        }
                    }
                });

        // 리스트로 변환하고 정렬하여 반환
        return uniqueTags.stream()
                .sorted()
                .collect(Collectors.toList());
    }
}

package com.jinjinjara.pola.vision.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinjinjara.pola.data.dto.response.CategoryIdResponse;
import com.jinjinjara.pola.data.dto.response.CategoryWithTagsResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.service.CategoryService;
import com.jinjinjara.pola.data.service.CategoryTagService;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.vision.dto.common.Result;
import com.jinjinjara.pola.vision.dto.common.VertexParsedResult;
import com.jinjinjara.pola.vision.dto.response.AnalyzeResponse;
import com.jinjinjara.pola.vision.dto.response.AnalyzeTestResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 전체 파이프라인 오케스트레이션:
 * S3 URL -> LLM 태그 -> 센트로이드(캐시 or 계산) -> 분류 -> 응답
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AnalyzeFacadeService {

    private final VertexService vertexService;
    private final CategoryEmbeddingService categoryEmbeddingService;
    private final EmbeddingCacheService embeddingCacheService;
    private final CategoryTagService categoryTagService;
    private final CategoryService categoryService;
    private final ClassifierService classifierService;

    private final ObjectMapper om = new ObjectMapper();

    public AnalyzeResponse analyze(Long userId, String s3Url) {

        // (1) Vertex
        String vertexBody = vertexService.analyzeImageFromUrl(s3Url);
        var parsed = parseVertexJson(vertexBody);
        List<String> inputTags = parsed.getTags();
        String description = parsed.getDescription();
        log.debug("[Analyze] Parsed from Vertex -> tags({}): {}, descLen={}",
                inputTags.size(), inputTags, description.length());

        if (inputTags.isEmpty()) {
            log.warn("[Analyze] No tags extracted. url={}", s3Url);
            return AnalyzeResponse.builder()
                    .categoryId(null).categoryName(null)
                    .tags(List.of()).description(description)
                    .build();
        }

        // (2) Centroids
        Map<String, float[]> centroids = loadCentroidsOrBuild(userId);
        log.debug("[Analyze] Centroids loaded. size={}", centroids.size());
        if (centroids.isEmpty()) {
            log.warn("[Analyze] No centroids for user={}", userId);
            return AnalyzeResponse.builder()
                    .categoryId(null).categoryName(null)
                    .tags(inputTags).description(description)
                    .build();
        }

        // (3) Category->Tags evidence
        Map<String, List<String>> categoryTags = loadCategoryTagsMap(userId);
        log.debug("[Analyze] CategoryTags loaded. size={}", categoryTags.size());

        // (4) Classify
        Result result = classifierService.classifyWithCentroids(inputTags, centroids, categoryTags, 3);
        String topCategory = result.getTopCategory();
        log.debug("[Analyze] Classify results top={}, scoresTopK={}",
                topCategory, result.getResults());

        // (5) categoryId 매핑
        CategoryIdResponse categoryInfo = categoryService.findCategoryIdByName(userId, topCategory);
        log.info("[Analyze] Matched category {}({}) for user={}",
                categoryInfo.getCategoryName(), categoryInfo.getCategoryId(), userId);

        // (6) 응답
        return AnalyzeResponse.builder()
                .categoryId(categoryInfo.getCategoryId())
                .categoryName(categoryInfo.getCategoryName())
                .tags(inputTags)
                .description(description)
                .build();
    }

    public AnalyzeTestResponse testAnalyze(Long userId, String s3Url) {

        int k = 3;

        // 1) LLM 호출 → 태그 뽑기
        String llmBody = vertexService.analyzeImageFromUrl(s3Url);
        List<String> inputTags = extractTagsFromGemini(llmBody);
        if (inputTags.isEmpty()) {
            log.warn("[Analyze] No tags extracted. url={}", s3Url);
            return AnalyzeTestResponse.builder()
                    .inputTags(List.of())
                    .topCategory(null)
                    .scores(List.of())
                    .topCategoryInputs(List.of())
                    .builtAt(null)
                    .build();
        }

        // 2) 센트로이드 로드(캐시) or 계산 & 저장
        Map<String, float[]> centroids = loadCentroidsOrBuild(userId);

        if (centroids.isEmpty()) {
            log.warn("[Analyze] No centroids available for user={}", userId);
            // 센트로이드가 없으면 분류 불가 → 최소 응답
            return AnalyzeTestResponse.builder()
                    .inputTags(inputTags)
                    .topCategory(null)
                    .scores(List.of())
                    .topCategoryInputs(List.of())
                    .builtAt(null)
                    .build();
        }

        // 3) evidence용 category -> tags 맵 구성 (DB 조회)
        Map<String, List<String>> categoryTags = loadCategoryTagsMap(userId);

        // 4) 분류 실행
        Result result = classifierService.classifyWithCentroids(
                inputTags,
                centroids,
                categoryTags,
                k
        );

        // 5) builtAt 메타 조회(있으면)
        String builtAt = embeddingCacheService.loadMetaJson(userId)
                .flatMap(meta -> {
                    try {
                        JsonNode n = om.readTree(meta);
                        JsonNode b = n.get("builtAt");
                        return Optional.ofNullable(b).map(JsonNode::asText);
                    } catch (Exception e) {
                        return Optional.empty();
                    }
                })
                .orElse(null);

        return AnalyzeTestResponse.builder()
                .inputTags(inputTags)
                .topCategory(result.getTopCategory())
                .scores(result.getResults())
                .topCategoryInputs(result.getTopCategoryInputs())
                .builtAt(builtAt)
                .build();
    }

    // ----------------------- helpers -----------------------

    /** 캐시에서 센트로이드 로드. 없으면 계산 후 저장(meta 포함). */
    private Map<String, float[]> loadCentroidsOrBuild(Long userId) {
        // 1) 캐시 로드
        var cached = embeddingCacheService.loadCentroidsJson(userId);
        if (cached.isPresent()) {
            try {
                Map<String, float[]> m = om.readValue(
                        cached.get(),
                        new TypeReference<Map<String, float[]>>() {}
                );
                if (m != null && !m.isEmpty()) {
                    return m;
                }
            } catch (Exception e) {
                log.warn("[Analyze] Failed to parse cached centroids. user={}", userId, e);
                // 캐시 파싱 실패 시 무시하고 재계산
            }
        }

        // 2) 미존재 or 파싱 실패 → 계산
        Map<String, float[]> centroids = categoryEmbeddingService.computeCategoryCentroids(userId);
        if (centroids.isEmpty()) {
            return Collections.emptyMap();
        }

        // 3) 저장
        try {
            String json = om.writeValueAsString(centroids);
            embeddingCacheService.saveCentroidsJson(userId, json);

            // meta 저장(옵션)
            Map<String, Object> meta = new HashMap<>();
            meta.put("builtAt", Instant.now().toString());
            meta.put("categoryCount", centroids.size());
            embeddingCacheService.saveMetaJson(userId, om.writeValueAsString(meta));
        } catch (Exception e) {
            log.warn("[Analyze] Failed to cache centroids. user={}", userId, e);
        }

        return centroids;
    }

    /** DB에서 사용자 카테고리 → 태그 목록을 불러와 evidence 맵 생성 */
    private Map<String, List<String>> loadCategoryTagsMap(Long userId) {
        Users user = Users.builder().id(userId).build();
        List<CategoryWithTagsResponse> list = categoryTagService.getUserCategoriesWithTags(user);
        if (list == null || list.isEmpty()) return Collections.emptyMap();

        Map<String, List<String>> map = new LinkedHashMap<>();
        for (CategoryWithTagsResponse c : list) {
            String category = c.getCategoryName();
            if (category == null || category.isBlank()) continue;

            List<String> tags = Optional.ofNullable(c.getTags()).orElse(List.of())
                    .stream()
                    .map(TagResponse::getTagName)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .collect(Collectors.toList());

            if (!tags.isEmpty()) map.put(category, tags);
        }
        return map;
    }

    /**
     * Gemini(GenerateContent) 응답 JSON에서 첫 번째 text를 꺼내
     * 그 내용이 JSON 배열 문자열이라 가정하고 파싱한다.
     * - prompt에서 "JSON 배열로만 출력"을 강제해둔 전제에 맞춘 파서
     */
    private List<String> extractTagsFromGemini(String body) {
        try {
            // 1) GenerateContent 표준 응답 파싱
            JsonNode root = om.readTree(body);

            // 후보가 여러개라도 첫 번째만 사용
            JsonNode candidates = root.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode content = candidates.get(0).get("content");
                if (content != null) {
                    JsonNode parts = content.get("parts");
                    if (parts != null && parts.isArray()) {
                        for (JsonNode p : parts) {
                            JsonNode t = p.get("text");
                            if (t != null && t.isTextual()) {
                                String text = t.asText().trim();
                                // text 자체가 JSON 배열 문자열이어야 함
                                return parseArrayString(text);
                            }
                        }
                    }
                }
            }

            // 2) 혹시 바로 배열 문자열이 온다면
            if (body.trim().startsWith("[")) {
                return parseArrayString(body.trim());
            }

        } catch (Exception e) {
            log.warn("[Analyze] Failed to parse LLM response", e);
        }
        return List.of();
    }

    private static final ObjectMapper JSON = new ObjectMapper();

    /** LLM이 돌려준 문자열에서 태그 배열을 안전하게 뽑아낸다. */
    private List<String> parseArrayString(String raw) {
        if (raw == null) return List.of();

        String s = raw.trim();

        // 1) 코드펜스 제거 ```...``` (언어 힌트 포함/미포함 모두)
        // ```json\n[ ... ]\n```  or  ```\n- 태그\n- 태그\n```
        if (s.startsWith("```")) {
            // (?s) = DOTALL. 펜스 내부만 추출
            var m = java.util.regex.Pattern
                    .compile("(?s)```[a-zA-Z0-9_-]*\\s*(.*?)\\s*```")
                    .matcher(s);
            if (m.find()) s = m.group(1).trim();
        }

        // 2) 양끝이 큰따옴표로 감싼 경우 언쿼트 ("[...]" 같은 케이스)
        if ((s.startsWith("\"") && s.endsWith("\"")) ||
                (s.startsWith("'") && s.endsWith("'"))) {
            s = s.substring(1, s.length() - 1).trim();
        }

        // 3) 대괄호 구간만 안전 슬라이스 (앞뒤에 설명 문장/프롬프트 잔재가 섞인 경우)
        int li = s.indexOf('[');
        int ri = s.lastIndexOf(']');
        if (li >= 0 && ri > li) {
            s = s.substring(li, ri + 1).trim();
        }

        // 4) JSON 배열 파싱 시도
        try {
            List<String> arr = JSON.readValue(s, new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {});
            return sanitizeTags(arr);
        } catch (Exception ignore) {
            // 계속 진행해서 폴백 파싱 시도
        }

        // 5) 폴백: 불릿/콤마/개행 기반 토크나이징
        // 예: "- 태그1\n- 태그2" or "태그1, 태그2"
        List<String> tokens = new ArrayList<>();
        for (String line : s.split("\\r?\\n")) {
            String t = line.trim();
            // 불릿 제거
            t = t.replaceFirst("^[*-]\\s*", "");
            // 라인에 콤마가 여러 개면 쪼개기
            if (t.contains(",")) {
                for (String part : t.split(",")) tokens.add(part.trim());
            } else {
                tokens.add(t);
            }
        }
        return sanitizeTags(tokens);
    }

    /** 공백/따옴표/빈 요소 정리 + 중복 제거 */
    private List<String> sanitizeTags(List<String> in) {
        if (in == null) return List.of();
        return in.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(t -> t.replaceAll("^['\"]|['\"]$", "")) // 양끝 따옴표 제거
                .filter(t -> !t.isEmpty())
                .distinct()
                .toList();
    }


    private String stripCodeFence(String s) {
        if (s == null) return null;
        String t = s.trim();
        if (t.startsWith("```")) {
            Matcher m = Pattern.compile("(?s)```[a-zA-Z0-9_-]*\\s*(.*?)\\s*```").matcher(t);
            if (m.find()) return m.group(1).trim();
        }
        return t;
    }

    // ② 객체 파서 강화 (기존 safeParseVertexObject 대체)
    private VertexParsedResult safeParseVertexObject(String text) {
        String raw = stripCodeFence(text);
        if (raw == null || raw.isBlank()) return new VertexParsedResult(List.of(), "");

        // (a) 그대로 시도
        try {
            JsonNode node = om.readTree(raw);
            List<String> tags = new ArrayList<>();
            if (node.has("tags") && node.get("tags").isArray()) node.get("tags").forEach(n -> tags.add(n.asText()));
            String desc = node.has("description") ? node.get("description").asText() : "";
            return new VertexParsedResult(sanitizeTags(tags), desc == null ? "" : desc.trim());
        } catch (Exception ignore) {}

        // (b) 중괄호 영역만 슬라이스해서 재시도
        int li = raw.indexOf('{');
        int ri = raw.lastIndexOf('}');
        if (li >= 0 && ri > li) {
            String obj = raw.substring(li, ri + 1);
            try {
                JsonNode node = om.readTree(obj);
                List<String> tags = new ArrayList<>();
                if (node.has("tags") && node.get("tags").isArray()) node.get("tags").forEach(n -> tags.add(n.asText()));
                String desc = node.has("description") ? node.get("description").asText() : "";
                return new VertexParsedResult(sanitizeTags(tags), desc == null ? "" : desc.trim());
            } catch (Exception ignore) {}
        }

        log.warn("[Analyze] Non-JSON vertex text after fence/brace handling: {}", raw);
        return new VertexParsedResult(List.of(), "");
    }

    // ③ parseVertexJson 안에서 스니펫 로그 + fence 처리 전진 배치
    private VertexParsedResult parseVertexJson(String body) {
        try {
            // 원문 스니펫 로그
            String snippet = body == null ? "null" : body.substring(0, Math.min(300, body.length()));
            log.debug("[Analyze] Vertex raw snippet: {}", snippet);

            JsonNode root = om.readTree(body);
            JsonNode candidates = root.get("candidates");
            if (candidates != null && candidates.isArray() && candidates.size() > 0) {
                JsonNode parts = candidates.get(0).path("content").path("parts");
                for (JsonNode p : parts) {
                    JsonNode t = p.get("text");
                    if (t != null && t.isTextual()) {
                        return safeParseVertexObject(t.asText());
                    }
                }
            }
            // fallback: body 자체가 fence 포함일 수도
            return safeParseVertexObject(body);

        } catch (Exception e) {
            log.warn("[Analyze] Failed to parse Vertex response JSON: {}", e.getMessage());
            return new VertexParsedResult(List.of(), "");
        }
    }
}

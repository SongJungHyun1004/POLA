package com.jinjinjara.pola.vision.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.jinjinjara.pola.vision.util.AIUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class VertexService {

    // ————— 환경설정 —————
    @Value("${gcp.project-id}") private String projectId;
    @Value("${vertex.location:us-central1}") private String location;

    @Value("${vertex.model.text:gemini-2.5-flash-lite}")
    private String textModel;

    @Value("${vertex.model.vision:gemini-2.5-flash-lite}")
    private String visionModel;

    // 다운로드 안전 가드
    private static final long MAX_IMAGE_BYTES = 100L * 1024 * 1024; // 100MB
    private static final long MAX_TEXT_BYTES  = 20L  * 1024 * 1024; // 20MB

    private final ObjectMapper om = new ObjectMapper();
    // Vertex 호출용
    private final RestTemplate rt = new RestTemplate();

    // ————— 공통 헬퍼 —————
    private String endpoint(String model) {
        return String.format(
                "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                location, projectId, location, model
        );
    }

    private String bearerToken() throws Exception {
        GoogleCredentials creds = GoogleCredentials.getApplicationDefault()
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        creds.refreshIfExpired();
        AccessToken t = creds.getAccessToken();
        if (t == null || t.getExpirationTime() == null || t.getExpirationTime().toInstant().isBefore(Instant.now())) {
            creds.refresh();
            t = creds.getAccessToken();
        }
        return "Bearer " + t.getTokenValue();
    }

    private HttpHeaders jsonHeaders() throws Exception {
        HttpHeaders h = new HttpHeaders();
        h.setContentType(MediaType.APPLICATION_JSON);
        h.set(HttpHeaders.AUTHORIZATION, bearerToken());
        return h;
    }

    private String postJson(String url, Map<String, Object> body) {
        try {
            HttpEntity<Map<String, Object>> req = new HttpEntity<>(body, jsonHeaders());
            ResponseEntity<String> res = rt.exchange(url, HttpMethod.POST, req, String.class);
            String resBody = res.getBody();
            log.debug("[VertexService] RESPONSE status={} len={} snippet={}",
                    res.getStatusCodeValue(),
                    resBody == null ? 0 : resBody.length(),
                    resBody == null ? "null" : resBody.substring(0, Math.min(300, resBody.length())));

            if (!res.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Vertex error: " + res.getStatusCodeValue() + " - " + resBody);
            }
            return resBody;
        } catch (Exception e) {
            log.error("Vertex call failed", e);
            throw new RuntimeException("Vertex call failed: " + e.getMessage(), e);
        }
    }

    // ————— 텍스트 → 태그 —————
    public String generateTagsFromText(String text) {
        if (text == null || text.isBlank()) {
            return "{\"error\":\"text is empty\"}";
        }

        String prompt = """
        너는 한국어 콘텐츠 분석기다.
        입력 문장에서 핵심적인 키워드(태그)와 간단한 설명을 함께 JSON으로만 출력해.
        설명은 4문장 이내로, 입력 내용의 주요 주제를 빠트리지 않게 요약해줘.
        없는 내용을 만들어내지 말고, 입력에 기반해서만 작성해.

        출력 형식 예시:
        {
          "tags": ["말차","초코","크림빵","디저트","간식"],
          "description": "연세 말차 크림빵 제품으로, 디저트 관련 콘텐츠입니다."
        }

        입력:
        """ + text;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 256
                )
        );

        String url = endpoint(textModel);
        return postJson(url, body);
    }

    // ————— 이미지 → 캡션/태그 —————
    public String analyzeImage(byte[] imageBytes) {
        if (imageBytes == null || imageBytes.length == 0) {
            return "{\"error\":\"image is empty\"}";
        }

        String b64 = Base64.encodeBase64String(imageBytes);
        String mime = AIUtil.sniffMime(imageBytes);

        String userText = """
        너는 한국어 이미지 분석기다.
        이미지 속의 글자(텍스트)와 시각적인 요소를 모두 참고해,
        핵심적인 키워드(태그)와 짧은 설명을 JSON 객체 하나로만 출력해.
        설명은 4문장 이내로 이미지의 주요 주제를 빠트리지 않게 요약해줘.
        없는 내용을 만들어내지 말고, 이미지 내용에 기반해서만 작성해.

        출력 형식 예시:
        {
          "tags": ["말차","초코","크림빵","디저트","간식"],
          "description": "연세 말차 크림빵 제품으로, 디저트 관련 콘텐츠입니다."
        }
        """;

        Map<String, Object> inlineImage = Map.of(
                "inlineData", Map.of(
                        "mimeType", mime,
                        "data", b64
                )
        );

        Map<String, Object> textPart = Map.of("text", userText);

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(inlineImage, textPart)
                )),
                "generationConfig", Map.of(
                        "temperature", 0.2,
                        "maxOutputTokens", 512
                )
        );

        String url = endpoint(visionModel);
        return postJson(url, body);
    }

    public String generateText(String prompt, double temperature, int maxTokens) {
        if (prompt == null || prompt.isBlank()) {
            return "{\"error\":\"prompt is empty\"}";
        }
        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                )),
                "generationConfig", Map.of(
                        "temperature", temperature,
                        "maxOutputTokens", maxTokens
                )
        );
        String url = endpoint(textModel);
        return postJson(url, body);
    }

    // ————— URL 입력 (이미지/텍스트 presigned 모두 처리, HEAD 없이 단일 GET) —————
    public String analyzeImageFromUrl(String url) throws Exception {
        if (url == null || url.isBlank() || !(url.startsWith("https://") || url.startsWith("http://"))) {
            return "{\"error\":\"invalid url\"}";
        }

        // presigned URL은 보통 GET 서명만 포함 → HEAD 금지, 단일 GET로 바이트 수신
        byte[] data = AIUtil.directDownloadBytes(url, MAX_IMAGE_BYTES);
        if (data == null || data.length == 0) {
            return "{\"error\":\"empty content\"}";
        }

        // 이미지 판별 (매직바이트)
        String mime = AIUtil.sniffMime(data);
        if (mime.startsWith("image/")) {
            return analyzeImage(data);
        }

        // 이미지 아니면 텍스트로 시도
        if (data.length > MAX_TEXT_BYTES) {
            return "{\"error\":\"text too large\"}";
        }
        String text = new String(data, StandardCharsets.UTF_8);
        if (text.isBlank()) {
            return "{\"error\":\"unsupported content\"}";
        }
        return generateTagsFromText(text);
    }
}
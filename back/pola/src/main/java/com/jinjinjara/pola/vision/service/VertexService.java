package com.jinjinjara.pola.vision.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Base64;
import java.util.List;
import java.util.Map;

/**
 * Vertex AI (Gemini 1.5) REST 호출 서비스
 * - 클라이언트 → Spring → Vertex AI REST API
 * - 항상 application/json으로 Vertex에 요청
 */
@Service
@RequiredArgsConstructor
public class VertexService {

    @Value("${gcp.project-id}")
    private String projectId;

    @Value("${gcp.location:asia-northeast3}")
    private String location; // 서울 리전 예시

    private static final String MODEL = "gemini-2.5-flash-lite-preview-09-2025";

    private final ObjectMapper objectMapper = new ObjectMapper();

    private String endpoint() {
        return String.format(
                "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/%s:generateContent",
                location, projectId, location, MODEL
        );
    }

    /** Google ADC(Service Account)로 액세스 토큰 발급 */
    private String getAccessToken() throws IOException {
        GoogleCredentials cred = GoogleCredentials.getApplicationDefault()
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));
        cred.refreshIfExpired();
        return cred.getAccessToken().getTokenValue();
    }

    /** 텍스트 → 태그 추출 (JSON 요청) */
    public String generateTagsFromText(String text) {
        try {
            if (text == null || text.isBlank()) {
                throw new IllegalArgumentException("text is required");
            }

            Map<String, Object> userPart = Map.of(
                    "role", "user",
                    "parts", List.of(Map.of("text", buildTagPrompt(text)))
            );
            Map<String, Object> body = Map.of(
                    "contents", List.of(userPart),
                    "generationConfig", Map.of(
                            "temperature", 0.2,
                            "responseMimeType", "application/json"
                    )
            );

            String json = objectMapper.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint()))
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() / 100 != 2) {
                throw new RuntimeException("Vertex error: " + resp.statusCode() + " - " + resp.body());
            }

            return extractTextFromGenerateContent(resp.body());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /** 이미지 → 설명 및 태그 추출 */
    public String analyzeImage(byte[] imageBytes) {
        try {
            String b64 = Base64.getEncoder().encodeToString(imageBytes);

            Map<String, Object> imagePart = Map.of(
                    "inline_data", Map.of(
                            "mime_type", "image/png",
                            "data", b64
                    )
            );

            Map<String, Object> userPart = Map.of(
                    "role", "user",
                    "parts", List.of(
                            Map.of("text", buildImagePrompt()),
                            imagePart
                    )
            );

            Map<String, Object> body = Map.of(
                    "contents", List.of(userPart),
                    "generationConfig", Map.of(
                            "temperature", 0.2,
                            "responseMimeType", "application/json"
                    )
            );

            String json = objectMapper.writeValueAsString(body);

            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint()))
                    .header("Authorization", "Bearer " + getAccessToken())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpClient client = HttpClient.newHttpClient();
            HttpResponse<String> resp = client.send(req, HttpResponse.BodyHandlers.ofString());

            if (resp.statusCode() / 100 != 2) {
                throw new RuntimeException("Vertex error: " + resp.statusCode() + " - " + resp.body());
            }

            return extractTextFromGenerateContent(resp.body());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* ======= 프롬프트 & 응답 파서 ======= */

    private String buildTagPrompt(String input) {
        return """
               너는 태그 생성기다. 아래 텍스트의 핵심을 3~8개의 한국어 태그로 뽑아라.
               금지: 해시(#), 이모지, 공백 많은 문자열
               출력 형식(반드시 JSON 배열): ["태그1","태그2",...]
               입력:
               """ + input;
    }

    private String buildImagePrompt() {
        return """
               이미지를 한국어로 한 줄 설명하고, 연관 태그 5~10개를 생성하라.
               오브젝트/브랜드/스타일/상황을 균형있게 뽑되 허상 금지.
               출력은 JSON:
               {
                 "caption": "설명문",
                 "tags": ["태그1","태그2", ...]
               }
               """;
    }

    /** candidates[0].content.parts[*].text 를 이어붙여 반환 */
    private String extractTextFromGenerateContent(String respJson) throws JsonProcessingException {
        JsonNode root = objectMapper.readTree(respJson);
        JsonNode candidates = root.path("candidates");
        if (!candidates.isArray() || candidates.isEmpty()) return "";

        StringBuilder sb = new StringBuilder();
        JsonNode parts = candidates.get(0).path("content").path("parts");
        if (parts.isArray()) {
            for (JsonNode p : parts) {
                if (p.has("text")) sb.append(p.get("text").asText());
            }
        }
        return sb.toString().trim();
    }
}
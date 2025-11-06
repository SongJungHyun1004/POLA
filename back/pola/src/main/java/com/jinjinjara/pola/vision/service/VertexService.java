package com.jinjinjara.pola.vision.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.ByteArrayInputStream;
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

    private final ObjectMapper om = new ObjectMapper();
    private final RestTemplate rt = new RestTemplate();

    // ————— 공통 헬퍼 —————
    private String endpoint(String model) {
        // https://{location}-aiplatform.googleapis.com/v1/projects/{project}/locations/{location}/publishers/google/models/{model}:generateContent
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
            if (!res.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Vertex error: " + res.getStatusCodeValue() + " - " + res.getBody());
            }
            return res.getBody();
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
                너는 한국어 태그 추출기다.
                입력 문장에서 핵심 키워드/해시태그 5~8개를 한국어로 JSON 배열로만 출력해.
                예) ["말차","초코","크림빵","디저트","간식"]

                입력:
                """ + text;

        Map<String, Object> body = Map.of(
                "contents", List.of(Map.of(
                        "role", "user",
                        "parts", List.of(Map.of("text", prompt))
                )),
                // 필요시 온도 등 파라미터:
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
        String mime = sniffMime(imageBytes);

        String userText =
                "이미지의 핵심 객체와 브랜드/제품명 단서를 찾아 한국어 태그 5~10개를 JSON 배열로만 출력해.";

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

    // URL 입력 어댑터 (힌트 제거 버전)
    public String analyzeImageFromUrl(String imageUrl) {
        byte[] bytes = downloadToBytes(imageUrl);
        return analyzeImage(bytes);
    }

    // 간단 다운로드 유틸 (필요시 타임아웃/크기 제한 추가)
    private byte[] downloadToBytes(String imageUrl) {
        try {
            return new RestTemplate().getForObject(imageUrl, byte[].class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to download image: " + imageUrl, e);
        }
    }

    private String sniffMime(byte[] img) {
        // 아주 단순 스니핑 (원하면 Apache Tika 등으로 교체)
        if (img.length >= 3 && img[0] == (byte)0xFF && img[1] == (byte)0xD8) return "image/jpeg";
        if (img.length >= 8 &&
                img[0] == (byte)0x89 && img[1] == 0x50 && img[2] == 0x4E && img[3] == 0x47) return "image/png";
        return "application/octet-stream";
    }
}
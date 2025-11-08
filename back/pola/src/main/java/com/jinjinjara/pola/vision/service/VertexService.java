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

import javax.net.ssl.HttpsURLConnection;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
    private static final long MAX_IMAGE_BYTES = 20L * 1024 * 1024; // 20MB
    private static final long MAX_TEXT_BYTES  = 2L  * 1024 * 1024; // 2MB

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
                입력 문장에서 핵심 키워드/해시태그를 한국어로 JSON 배열로만 출력해.
                중요한 키워드는 가능한 뽑아내되 없는 내용을 만들 필요는 없어.
                예) ["말차","초코","크림빵","디저트","간식"]

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
        String mime = sniffMime(imageBytes);

        String userText = """
                        먼저 이미지에서 글자라고 생각되는 부분을 모두 읽고 
                        중요한 부분이나 주제라고 생각되는 키워드를 분석해서 한국어 태그를 JSON 배열로만 출력해.
                        핵심적인 키워드를 가능한 뽑아내되 없는 내용을 만들 필요는 없어.
                        예) ["말차","초코","크림빵","디저트","간식"]
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

    // ————— URL 입력 (이미지/텍스트 presigned 모두 처리, HEAD 없이 단일 GET) —————
    public String analyzeImageFromUrl(String url) {
        if (url == null || url.isBlank() || !(url.startsWith("https://") || url.startsWith("http://"))) {
            return "{\"error\":\"invalid url\"}";
        }

        // presigned URL은 보통 GET 서명만 포함 → HEAD 금지, 단일 GET로 바이트 수신
        byte[] data = directDownloadBytes(url, MAX_IMAGE_BYTES);
        if (data == null || data.length == 0) {
            return "{\"error\":\"empty content\"}";
        }

        // 이미지 판별 (매직바이트)
        String mime = sniffMime(data);
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

    // ————— 순수 JDK 다운로드 (헤더 파싱 무의존) —————
    private byte[] directDownloadBytes(String urlStr, long maxBytes) {
        HttpURLConnection conn = null;
        try {
            URL url = new URL(urlStr);
            conn = (HttpURLConnection) url.openConnection();
            if (conn instanceof HttpsURLConnection https) {
                https.setInstanceFollowRedirects(true);
            }
            conn.setInstanceFollowRedirects(true);
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(20000);
            conn.setRequestProperty("User-Agent", "pola-vertex-downloader/1.0");

            int code = conn.getResponseCode();
            InputStream is = (code >= 200 && code < 400) ? conn.getInputStream() : conn.getErrorStream();
            if (is == null) throw new RuntimeException("No response stream, status=" + code);

            long declared = conn.getContentLengthLong(); // -1 가능
            if (declared > 0 && declared > maxBytes) {
                throw new RuntimeException("Object too large (Content-Length): " + declared);
            }

            byte[] data = is.readAllBytes(); // JDK 11+
            if (data.length > maxBytes) {
                throw new RuntimeException("Object too large (actual): " + data.length);
            }
            if (code < 200 || code >= 300) {
                String snippet = new String(data, 0, Math.min(256, data.length), StandardCharsets.UTF_8);
                log.error("[VertexService] GET {} -> {} bodySnippet={}", urlStr, code, snippet);
                throw new RuntimeException("HTTP " + code);
            }
            return data;

        } catch (Exception e) {
            log.error("[VertexService] directDownloadBytes failed: {}", e.toString());
            throw new RuntimeException("Failed to download image: " + urlStr, e);
        } finally {
            if (conn != null) conn.disconnect();
        }
    }

    // ————— 매직바이트로 MIME 추정 —————
    private String sniffMime(byte[] img) {
        // JPEG
        if (img.length >= 3 && img[0] == (byte)0xFF && img[1] == (byte)0xD8) return "image/jpeg";
        // PNG
        if (img.length >= 8 &&
                img[0] == (byte)0x89 && img[1] == 0x50 && img[2] == 0x4E && img[3] == 0x47) return "image/png";
        // WEBP: "RIFF....WEBP"
        if (img.length >= 12 &&
                img[0] == 'R' && img[1] == 'I' && img[2] == 'F' && img[3] == 'F' &&
                img[8] == 'W' && img[9] == 'E' && img[10] == 'B' && img[11] == 'P') return "image/webp";
        // GIF: "GIF87a"/"GIF89a"
        if (img.length >= 6 &&
                img[0] == 'G' && img[1] == 'I' && img[2] == 'F' && img[3] == '8' &&
                (img[4] == '7' || img[4] == '9') && img[5] == 'a') return "image/gif";

        return "application/octet-stream";
    }
}
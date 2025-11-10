package com.jinjinjara.pola.vision.service;

import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import com.jinjinjara.pola.vision.dto.common.Instance;
import com.jinjinjara.pola.vision.dto.common.Prediction;
import com.jinjinjara.pola.vision.dto.request.PredictRequest;
import com.jinjinjara.pola.vision.dto.response.PredictResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.Normalizer;

@Slf4j
@Service
public class EmbeddingService {

    @Value("${google.project-id}")
    private String project;

    @Value("${vertex.location}")
    private String location;

    @Value("${embedding.chunk.maxChars:8000}")
    private int maxChars;

    @Value("${embedding.chunk.window:3000}")
    private int windowChars;

    @Value("${embedding.chunk.overlap:400}")
    private int overlapChars;

    private static final List<String> SCOPES =
            List.of("https://www.googleapis.com/auth/cloud-platform");

    private final RestClient rest = RestClient.create();

    public float[] embedOcrAndContext(String ocrText, String context) {
        String combined = combineAndNormalize(ocrText, context);
        if (combined.isBlank()) throw new IllegalArgumentException("empty input");

        // 길이 상한 컷
        if (combined.length() > maxChars) {
            combined = combined.substring(0, maxChars);
        }

        List<String> chunks = chunkByChars(combined, windowChars, overlapChars);
        List<float[]> vs = embedTexts(chunks);
        if (vs.isEmpty()) throw new RuntimeException("Empty vectors");

        return mean(vs);
    }

    /**
     * 입력 문자열 리스트를 Vertex AI 임베딩 벡터로 변환.
     * 모델: text-multilingual-embedding-002
     */
    public List<float[]> embedTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) return List.of();

        String url = String.format(
                "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/text-multilingual-embedding-002:predict",
                location, project, location
        );

        List<Instance> instances = new ArrayList<>();
        for (String t : texts) {
            instances.add(new Instance(t)); // DTO의 content 필드에 매핑
        }
        PredictRequest body = new PredictRequest(instances);

        PredictResponse res = rest.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token())
                .body(body)
                .retrieve()
                .body(PredictResponse.class);

        if (res == null || res.getPredictions() == null || res.getPredictions().isEmpty()) {
            throw new RuntimeException("Empty embedding response");
        }

        List<float[]> out = new ArrayList<>(res.getPredictions().size());
        for (Prediction p : res.getPredictions()) {
            var values = p.getEmbeddings().getValues();
            float[] v = new float[values.size()];
            for (int i = 0; i < v.length; i++) v[i] = values.get(i).floatValue();
            out.add(v);
        }
        return out;
    }

    /** 여러 벡터의 단순 평균 */
    public float[] embedMean(List<String> texts) {
        if (texts == null || texts.isEmpty()) return null;
        List<float[]> vs = embedTexts(texts);
        return (vs.isEmpty() ? null : mean(vs));
    }

    // ----------------- internal helpers -----------------

    private String token() {
        try {
            GoogleCredentials gc = GoogleCredentials.getApplicationDefault().createScoped(SCOPES);
            gc.refreshIfExpired();
            AccessToken t = gc.getAccessToken();
            return t.getTokenValue();
        } catch (IOException e) {
            throw new RuntimeException("Failed to obtain Google access token", e);
        }
    }

    private static float[] mean(List<float[]> vs) {
        int d = vs.get(0).length;
        float[] m = new float[d];
        for (float[] v : vs) {
            for (int i = 0; i < d; i++) m[i] += v[i];
        }
        for (int i = 0; i < d; i++) m[i] /= vs.size();
        return m;
    }

    private static String combineAndNormalize(String ocrText, String context) {
        String a = (context == null) ? "" : context;
        String b = (ocrText == null) ? "" : ocrText;

        String s = (a.isBlank() ? "" : a.strip() + "\n\n") + b.strip();

        // 공백 정리 및 제어문자 제거
        s = s.replaceAll("\\p{Cntrl}", " ")
                .replaceAll("[ \\t\\x0B\\f\\r]+", " ")
                .replaceAll("\\n{3,}", "\n\n")
                .trim();

        // 정규화(NFKC)
        return Normalizer.normalize(s, Normalizer.Form.NFKC);
    }

    private static List<String> chunkByChars(String s, int window, int overlap) {
        List<String> out = new ArrayList<>();
        if (s.length() <= window) {
            out.add(s);
            return out;
        }
        int step = Math.max(1, window - Math.max(0, overlap));
        int i = 0;
        while (i < s.length()) {
            int end = Math.min(s.length(), i + window);
            out.add(s.substring(i, end));
            if (end == s.length()) break;
            i += step;
        }
        return out;
    }
}
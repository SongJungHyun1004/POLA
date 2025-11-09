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

@Slf4j
@Service
public class EmbeddingService {

    @Value("${google.project-id}")
    private String project;

    @Value("${vertex.location}")
    private String location;

    private static final List<String> SCOPES =
            List.of("https://www.googleapis.com/auth/cloud-platform");

    private final RestClient rest = RestClient.create();

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
}
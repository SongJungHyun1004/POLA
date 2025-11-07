package com.jinjinjara.pola.vision.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.auth.oauth2.AccessToken;
import com.google.auth.oauth2.GoogleCredentials;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import static java.util.stream.Collectors.toList;

@Slf4j
@Service
public class EmbeddingService {

    @Value("${google.project-id}") private String project;
    @Value("${vertex.location}")   private String location;

    private static final List<String> SCOPES = List.of("https://www.googleapis.com/auth/cloud-platform");
    private final RestClient rest = RestClient.create();
    private final AtomicBoolean warmedUp = new AtomicBoolean(false);

    // ================== 테스트용 카테고리/태그 ==================
    private final Map<String, List<String>> CATEGORY_TAGS = Map.of(
            "한식", List.of("삼겹살","백반","찌개","김치","불고기","비빔밥","반찬","국밥"),
            "중식", List.of("사천","짜장면","짬뽕","탕수육","마라","볶음밥","깐풍기","중국요리"),
            "일식", List.of("회","초밥","사시미","돈까스","라멘","우동","규동","일본요리"),
            "양식", List.of("리조또","스테이크","파스타","피자","치즈","버거","감자튀김","프랑스요리"),
            "분식", List.of("쫄면","떡볶이","김밥","순대","튀김","어묵","라면","핫도그"),
            "술집", List.of("막걸리","회식","맥주","소주","와인","포차","호프집","이자카야"),
            "카페", List.of("샌드위치","커피","디저트","케이크","브런치","라떼","빵","원두"),
            "배달", List.of("치킨","피자","햄버거","족발","보쌈","짜장면","떡볶이","야식")
    );

    private final Map<String, float[]> tagVec = new HashMap<>();
    private final Map<String, float[]> catCentroid = new HashMap<>();

    // ================== 초기화 ==================
    @PostConstruct
    public void warmup() {
        try {
            rebuild();
            warmedUp.set(true);
            log.info("[EmbeddingService] Warmup complete: {} tags, {} categories", tagVec.size(), catCentroid.size());
        } catch (Exception e) {
            log.error("[EmbeddingService] Warmup failed: {}", e.toString());
        }
    }

    public synchronized void rebuild() {
        List<String> all = CATEGORY_TAGS.values().stream().flatMap(List::stream).distinct().collect(toList());
        List<float[]> vecs = embedTexts(all);

        tagVec.clear();
        for (int i = 0; i < all.size(); i++) tagVec.put(all.get(i), vecs.get(i));

        catCentroid.clear();
        for (var e : CATEGORY_TAGS.entrySet()) {
            List<float[]> mats = e.getValue().stream().map(tagVec::get).toList();
            catCentroid.put(e.getKey(), mean(mats));
        }
    }

    private void ensureReady() {
        if (!warmedUp.get() || tagVec.isEmpty() || catCentroid.isEmpty()) {
            synchronized (this) {
                if (!warmedUp.get() || tagVec.isEmpty() || catCentroid.isEmpty()) {
                    rebuild();
                    warmedUp.set(true);
                }
            }
        }
    }

    // ================== 외부 접근 메서드 ==================
    public Set<String> categories() { ensureReady(); return CATEGORY_TAGS.keySet(); }
    public List<String> tagsOf(String cat) { ensureReady(); return CATEGORY_TAGS.getOrDefault(cat, List.of()); }
    public float[] tagVector(String tag) { ensureReady(); return tagVec.get(tag); }
    public float[] categoryCentroid(String cat) { ensureReady(); return catCentroid.get(cat); }

    public float[] embedMean(List<String> tags) {
        if (tags == null || tags.isEmpty()) return null;
        List<float[]> vs = embedTexts(tags);
        return mean(vs);
    }

    // ================== Vertex AI 호출 ==================
    public List<float[]> embedTexts(List<String> texts) {
        if (texts == null || texts.isEmpty()) return List.of();

        String url = String.format(
                "https://%s-aiplatform.googleapis.com/v1/projects/%s/locations/%s/publishers/google/models/text-multilingual-embedding-002:predict",
                location, project, location
        );

        List<Instance> instances = new ArrayList<>();
        for (String t : texts) instances.add(new Instance(t)); // content 필드 필수
        PredictRequest body = new PredictRequest(instances);

        PredictResponse res = rest.post()
                .uri(url)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", "Bearer " + token())
                .body(body)
                .retrieve()
                .body(PredictResponse.class);

        if (res == null || res.predictions == null || res.predictions.isEmpty())
            throw new RuntimeException("Empty embedding response");

        List<float[]> out = new ArrayList<>();
        for (Prediction p : res.predictions) {
            float[] v = new float[p.embeddings.values.size()];
            for (int i = 0; i < v.length; i++) v[i] = p.embeddings.values.get(i).floatValue();
            out.add(v);
        }
        return out;
    }

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
        int d = vs.get(0).length; float[] m = new float[d];
        for (float[] v : vs) for (int i = 0; i < d; i++) m[i] += v[i];
        for (int i = 0; i < d; i++) m[i] /= vs.size();
        return m;
    }

    // ================== DTO ==================
    record PredictRequest(List<Instance> instances) {}
    record Instance(@JsonProperty("content") String content) {}
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class PredictResponse { public List<Prediction> predictions; }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Prediction { public Embedding embeddings; }
    @JsonIgnoreProperties(ignoreUnknown = true)
    static class Embedding { public List<Double> values; }
}
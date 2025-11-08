package com.jinjinjara.pola.vision.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinjinjara.pola.vision.dto.common.Evidence;
import com.jinjinjara.pola.vision.dto.common.InputRel;
import com.jinjinjara.pola.vision.dto.common.Result;
import com.jinjinjara.pola.vision.dto.common.Score;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ClassifierService {

    private final EmbeddingService embedding;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    // ---- 튜닝 파라미터 ----
    @Value("${classifier.alpha:0.7}")
    private double alpha;                // (예비) 블렌딩 비율
    @Value("${classifier.generic.downscale:0.3}")
    private double genericDown;          // 프로모션성 입력/증거 다운가중
    @Value("${classifier.input.min-sim:0.35}")
    private double minInputSim;          // 입력-카테고리 최소 유사도 컷
    @Value("${classifier.evidence.top:3}")
    private int evidenceTopN;            // 카테고리 근거 태그 Top-N
    @Value("${classifier.topInputN:5}")
    private int topInputN;               // 1위 카테고리와 가까운 입력 Top-N
    @Value("${classifier.topInputs.skip-generic:false}")
    private boolean skipGenericInTopInputs; // true면 TopInputs에서 generic 제외

    // ---- 사전 파일 경로 ----
    @Value("${nlp.paths.synonyms:classpath:nlp/synonyms.yml}")
    private String synonymsPath;
    @Value("${nlp.paths.generic:classpath:nlp/generic.yml}")
    private String genericPath;

    // ---- 메모리 사전 (yml에서만 로드; 기본값 없음) ----
    private Map<String,String> SYNONYM = Map.of();
    private Set<String> GENERIC = Set.of();

    public ClassifierService(EmbeddingService embedding, ResourceLoader rl) {
        this.embedding = embedding;
        this.resourceLoader = rl;
    }

    @PostConstruct
    private void init() {
        loadDictionaries();
    }

    private void loadDictionaries() {
        // synonyms.yml → Map<String,String>
        try {
            var res = resourceLoader.getResource(synonymsPath);
            if (res.exists()) {
                SYNONYM = yaml.readValue(res.getInputStream(), new TypeReference<Map<String,String>>(){});
            }
        } catch (Exception e) {
            System.out.println("[Classifier] failed to load synonyms: " + e.getMessage());
        }

        // generic.yml → List<String> → Set
        try {
            var res = resourceLoader.getResource(genericPath);
            if (res.exists()) {
                List<String> g = yaml.readValue(res.getInputStream(), new TypeReference<List<String>>(){});
                GENERIC = g.stream().map(this::normalize).filter(s->!s.isEmpty())
                        .collect(Collectors.toCollection(LinkedHashSet::new));
            }
        } catch (Exception e) {
            System.out.println("[Classifier] failed to load generic: " + e.getMessage());
        }

        System.out.printf("[Classifier] dicts loaded: synonyms=%d, generic=%d (paths: syn=%s, gen=%s)%n",
                SYNONYM.size(), GENERIC.size(), synonymsPath, genericPath);
    }

    // ========================= 외부 센트로이드/카테고리-태그 기반 분류 =========================
    public Result classifyWithCentroids(
            List<String> inputTags,
            Map<String, float[]> centroids,                 // category -> centroid
            Map<String, List<String>> categoryTags,         // category -> tags (evidence 계산용)
            Integer topk                                     // null이면 기본 3
    ) {
        if (inputTags == null || inputTags.isEmpty()) {
            return new Result(List.of(), null, List.of());
        }
        if (centroids == null || centroids.isEmpty()) {
            return new Result(List.of(), null, List.of());
        }

        // 1) 정규화 + 동의어 치환
        List<String> canonInputs = inputTags.stream()
                .map(this::canonicalize)
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (canonInputs.isEmpty()) {
            return new Result(List.of(), null, List.of());
        }

        for (String t : canonInputs) {
            if (GENERIC.contains(t)) {
                System.out.println("[Classifier] generic hit (input): " + t);
            }
        }

        // 2) 입력 태그 임베딩 + 가중치
        double[] weights = new double[canonInputs.size()];
        for (int i = 0; i < canonInputs.size(); i++) {
            String t = canonInputs.get(i);
            weights[i] = GENERIC.contains(t) ? genericDown : 1.0;
        }
        List<float[]> inputVecs = embedding.embedTexts(canonInputs);
        float[] q = weightedMean(inputVecs, weights);

        // 3) 근거 태그 임베딩(전체 카테고리 태그를 한 번에 배치 임베딩)
        Map<String, float[]> tagVec = Collections.emptyMap();
        if (categoryTags != null && !categoryTags.isEmpty()) {
            LinkedHashSet<String> allTags = new LinkedHashSet<>();
            categoryTags.values().forEach(list -> {
                if (list != null) {
                    list.stream()
                            .filter(Objects::nonNull)
                            .map(String::trim)
                            .filter(s -> !s.isEmpty())
                            .forEach(allTags::add);
                }
            });
            if (!allTags.isEmpty()) {
                List<String> tagList = new ArrayList<>(allTags);
                List<float[]> tagEmbeds = embedding.embedTexts(tagList);
                Map<String, float[]> m = new HashMap<>(tagList.size());
                for (int i = 0; i < tagList.size(); i++) {
                    float[] v = tagEmbeds.get(i);
                    if (v != null && v.length > 0) m.put(tagList.get(i), v);
                }
                tagVec = m;
            }
        }

        // 4) 카테고리 점수 및 근거 태그
        List<Score> scores = new ArrayList<>();
        for (var e : centroids.entrySet()) {
            String cat = e.getKey();
            float[] centroid = e.getValue();
            if (centroid == null || centroid.length == 0) continue;

            double sim = cosine(q, centroid);

            List<Evidence> ev = new ArrayList<>();
            List<String> tags = (categoryTags == null) ? null : categoryTags.get(cat);
            if (tags != null && !tags.isEmpty() && !tagVec.isEmpty()) {
                for (String t : tags) {
                    String tt = (t == null) ? null : t.trim();
                    if (tt == null || tt.isEmpty()) continue;
                    float[] tv = tagVec.get(tt);
                    if (tv != null) {
                        double s = cosine(q, tv);
                        if (GENERIC.contains(tt)) s *= genericDown;
                        ev.add(new Evidence(tt, s));
                    }
                }
                ev.sort((a,b)->Double.compare(b.getSimilarity(), a.getSimilarity()));
                if (ev.size() > evidenceTopN) ev = ev.subList(0, evidenceTopN);
            }

            scores.add(new Score(cat, sim, ev));
        }
        if (scores.isEmpty()) {
            return new Result(List.of(), null, List.of());
        }
        scores.sort((a,b)->Double.compare(b.getSimilarity(), a.getSimilarity()));

        int k = (topk == null ? 3 : topk);
        if (k < 1) k = 1;
        if (k > scores.size()) k = scores.size();
        List<Score> topScores = scores.subList(0, k);

        // 5) 1위 카테고리에 대한 입력 태그별 유사도 Top-N
        String topCategory = topScores.get(0).getCategory();
        float[] topCentroid = centroids.get(topCategory);

        List<InputRel> topCategoryInputs = new ArrayList<>();
        for (int i = 0; i < canonInputs.size(); i++) {
            String tag = canonInputs.get(i);
            if (skipGenericInTopInputs && GENERIC.contains(tag)) {
                System.out.println("[Classifier] skip generic in topInputs: " + tag);
                continue;
            }
            double s = cosine(inputVecs.get(i), topCentroid);
            if (GENERIC.contains(tag)) s *= genericDown;
            if (s >= minInputSim) topCategoryInputs.add(new InputRel(tag, s));
        }
        topCategoryInputs.sort((a,b)->Double.compare(b.getSimilarity(), a.getSimilarity()));
        if (topCategoryInputs.size() > topInputN) {
            topCategoryInputs = topCategoryInputs.subList(0, topInputN);
        }

        return new Result(topScores, topCategory, topCategoryInputs);
    }

    // ========================= 수학/유틸 =========================
    private static float[] weightedMean(List<float[]> xs, double[] w) {
        int d = xs.get(0).length;
        float[] m = new float[d];
        double sum = 0.0;
        for (int i = 0; i < xs.size(); i++) {
            float[] v = xs.get(i);
            double ww = w[i];
            sum += ww;
            for (int j=0;j<d;j++) m[j] += v[j] * ww;
        }
        if (sum == 0) return xs.get(0);
        for (int j=0;j<d;j++) m[j] /= (float)sum;
        return m;
    }

    private static double cosine(float[] a, float[] b) {
        double dot=0,na=0,nb=0; for(int i=0;i<a.length;i++){dot+=a[i]*b[i]; na+=a[i]*a[i]; nb+=b[i]*b[i];}
        return (na==0||nb==0)?0: dot/(Math.sqrt(na)*Math.sqrt(nb));
    }

    private String canonicalize(String s) {
        String t = normalize(s);
        if (SYNONYM.containsKey(t)) t = normalize(SYNONYM.get(t)); // 동의어 치환
        return t;
    }

    private String normalize(String s) {
        if (s == null) return "";
        String t = s.trim();
        return Normalizer.normalize(t, Normalizer.Form.NFC);
    }
}
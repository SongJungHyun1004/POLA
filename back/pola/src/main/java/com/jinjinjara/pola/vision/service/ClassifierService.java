package com.jinjinjara.pola.vision.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
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

    // ---- 튜닝 파라미터 (application.yml 없으면 기본값) ----
    @Value("${classifier.alpha:0.7}")                   private double alpha;                // (예비) 센트로이드/태그 블렌딩 비율
    @Value("${classifier.generic.downscale:0.3}")       private double genericDown;          // generic(프로모션성) 입력/증거 다운가중
    @Value("${classifier.input.min-sim:0.35}")          private double minInputSim;          // 입력-카테고리 최소 유사도 컷
    @Value("${classifier.evidence.top:3}")              private int evidenceTopN;            // 카테고리 근거 태그 Top-N
    @Value("${classifier.topInputN:5}")                 private int topInputN;               // 1위 카테고리와 가까운 입력 Top-N
    @Value("${classifier.topInputs.skip-generic:false}") private boolean skipGenericInTopInputs; // true면 TopInputs에서 generic 제외

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
        // 주의: 여기서 사전을 로드하면 @Value 주입 전이라 경로가 기본값/빈 값일 수 있음.
        // 반드시 @PostConstruct에서 로드한다.
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

    // ========================= 분류 본체 =========================
    public Result classify(List<String> inputTags, int topk) {
        if (inputTags == null || inputTags.isEmpty()) {
            return new Result(List.of(), null, List.of());
        }

        // 1) 정규화 + 동의어 치환
        List<String> canonInputs = inputTags.stream()
                .map(this::canonicalize)               // trim + NFC + synonyms
                .filter(s -> !s.isEmpty())
                .distinct()
                .toList();

        if (canonInputs.isEmpty()) {
            return new Result(List.of(), null, List.of());
        }

        // 디버그: 어떤 입력이 generic으로 인식되는지 확인
        // (배포 후엔 로그 레벨에 따라 끄면 됨)
        for (String t : canonInputs) {
            if (GENERIC.contains(t)) {
                System.out.println("[Classifier] generic hit (input): " + t);
            }
        }

        // 2) 입력 태그 임베딩 + 가중치(일반 키워드 다운가중)
        double[] weights = new double[canonInputs.size()];
        for (int i = 0; i < canonInputs.size(); i++) {
            String t = canonInputs.get(i);
            weights[i] = GENERIC.contains(t) ? genericDown : 1.0; // generic이면 다운가중
        }
        List<float[]> inputVecs = embedding.embedTexts(canonInputs);

        // 가중 평균 쿼리 벡터 q
        float[] q = weightedMean(inputVecs, weights);

        // 3) 카테고리 점수 + 근거 태그(카테고리 내부 태그 기준)
        List<Score> scores = new ArrayList<>();
        for (String cat : embedding.categories()) {
            double sim = cosine(q, embedding.categoryCentroid(cat));

            List<Evidence> ev = new ArrayList<>();
            for (String t : embedding.tagsOf(cat)) {
                float[] tv = embedding.tagVector(t);
                if (tv != null) {
                    double s = cosine(q, tv);
                    // 근거 태그에도 generic 다운가중(해당 도메인에 generic이 있을 경우)
                    if (GENERIC.contains(t)) s *= genericDown;
                    ev.add(new Evidence(t, s));
                }
            }
            ev.sort((a,b)->Double.compare(b.similarity, a.similarity));
            if (ev.size() > evidenceTopN) ev = ev.subList(0, evidenceTopN);

            scores.add(new Score(cat, sim, ev));
        }
        scores.sort((a,b)->Double.compare(b.similarity, a.similarity));

        if (topk < 1) topk = 1;
        if (topk > scores.size()) topk = scores.size();
        List<Score> topScores = scores.subList(0, topk);

        // 4) 최종 1위 카테고리에 대해: 각 "입력 태그" vs "카테고리 센트로이드" 유사도 Top-N
        String topCategory = topScores.get(0).category();
        float[] topCentroid = embedding.categoryCentroid(topCategory);

        List<InputRel> topCategoryInputs = new ArrayList<>();
        for (int i = 0; i < canonInputs.size(); i++) {
            String tag = canonInputs.get(i);

            // 옵션: generic 입력은 TopInputs에서 제외
            if (skipGenericInTopInputs && GENERIC.contains(tag)) {
                System.out.println("[Classifier] skip generic in topInputs: " + tag);
                continue;
            }

            double s = cosine(inputVecs.get(i), topCentroid);

            // 핵심: 개별 입력 랭킹에도 generic 다운가중 적용
            if (GENERIC.contains(tag)) {
                s *= genericDown;
            }

            if (s >= minInputSim) {
                topCategoryInputs.add(new InputRel(tag, s));
            }
        }
        topCategoryInputs.sort((a,b)->Double.compare(b.similarity, a.similarity));
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

    // ========================= DTO =========================
    public record Evidence(String tag, double similarity) {}
    public record Score(String category, double similarity, List<Evidence> topTags) {}
    /** 최종 1위 카테고리에 대해, 각 입력 태그가 그 카테고리에 얼마나 가까운지 */
    public record InputRel(String inputTag, double similarity) {}
    public record Result(List<Score> results, String topCategory, List<InputRel> topCategoryInputs) {}
}
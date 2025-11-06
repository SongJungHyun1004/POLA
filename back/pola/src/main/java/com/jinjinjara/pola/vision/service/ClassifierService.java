package com.jinjinjara.pola.vision.service;

import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class ClassifierService {

    private final EmbeddingService embedding;

    public ClassifierService(EmbeddingService embedding) {
        this.embedding = embedding;
    }

    public Result classify(List<String> inputTags, int topk) {
        if (inputTags == null || inputTags.isEmpty()) {
            return new Result(List.of());
        }

        // 입력 태그 평균 벡터
        float[] q = embedding.embedMean(inputTags);
        if (q == null) return new Result(List.of());

        List<Score> scores = new ArrayList<>();

        for (String cat : embedding.categories()) {
            double sim = cosine(q, embedding.categoryCentroid(cat));

            // 카테고리 내 근거 태그 상위 3개
            List<Evidence> ev = new ArrayList<>();
            for (String t : embedding.tagsOf(cat)) {
                float[] tv = embedding.tagVector(t);
                if (tv != null) {
                    ev.add(new Evidence(t, cosine(q, tv)));
                }
            }
            ev.sort(Comparator.comparingDouble((Evidence e) -> e.similarity).reversed());
            if (ev.size() > 3) ev = ev.subList(0, 3);

            scores.add(new Score(cat, sim, ev));
        }

        scores.sort(Comparator.comparingDouble((Score s) -> s.similarity).reversed());
        if (topk < 1) topk = 1;
        if (topk > scores.size()) topk = scores.size();

        return new Result(scores.subList(0, topk));
    }

    // --- math ---
    private static double cosine(float[] a, float[] b) {
        double dot = 0, na = 0, nb = 0;
        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            na  += a[i] * a[i];
            nb  += b[i] * b[i];
        }
        return (na == 0 || nb == 0) ? 0 : dot / (Math.sqrt(na) * Math.sqrt(nb));
    }

    // --- DTOs ---
    public record Evidence(String tag, double similarity) {}
    public record Score(String category, double similarity, List<Evidence> topTags) {}
    public record Result(List<Score> results) {}
}
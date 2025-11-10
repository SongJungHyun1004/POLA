package com.jinjinjara.pola.vision.service;

import com.jinjinjara.pola.data.dto.response.CategoryWithTagsResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.service.CategoryTagService;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryEmbeddingService {

    private static final int MAX_UNIQUE_TAGS = 2000; // 안전 상한 (필요시 조정)
    private final CategoryTagService categoryTagService;
    private final EmbeddingService embeddingService;


    public Map<String, float[]> computeCategoryCentroids(Long userId) {
        // 1) 유저 카테고리/태그 조회
        Users user = Users.builder().id(userId).build();
        List<CategoryWithTagsResponse> categories = categoryTagService.getUserCategoriesWithTags(user);
        if (categories == null || categories.isEmpty()) {
            return Collections.emptyMap();
        }

        // 2) 전체 고유 태그 수집 (순서 보존)
        LinkedHashSet<String> uniqueTags = new LinkedHashSet<>();
        for (CategoryWithTagsResponse c : categories) {
            if (c.getTags() == null) continue;
            for (TagResponse t : c.getTags()) {
                String name = (t == null) ? null : t.getTagName();
                if (name != null) {
                    String trimmed = name.trim();
                    if (!trimmed.isEmpty()) uniqueTags.add(trimmed);
                }
            }
        }
        if (uniqueTags.isEmpty()) {
            return Collections.emptyMap();
        }

        // 상한 적용 (너무 클 경우 앞에서부터 자른다)
        List<String> tagList = new ArrayList<>(uniqueTags);
        if (tagList.size() > MAX_UNIQUE_TAGS) {
            tagList = tagList.subList(0, MAX_UNIQUE_TAGS);
        }

        // 3) 배치 임베딩
        List<float[]> vectors = embeddingService.embedTexts(tagList);
        if (vectors == null || vectors.isEmpty()) {
            return Collections.emptyMap();
        }
        // 태그명 -> 임베딩 벡터 맵
        Map<String, float[]> tagVec = new HashMap<>(tagList.size());
        for (int i = 0; i < tagList.size(); i++) {
            float[] v = vectors.get(i);
            if (v != null && v.length > 0) {
                tagVec.put(tagList.get(i), v);
            }
        }

        // 4) 카테고리별 평균(centroid) 계산
        Map<String, float[]> centroids = new LinkedHashMap<>();
        for (CategoryWithTagsResponse c : categories) {
            String categoryName = c.getCategoryName();
            if (categoryName == null || categoryName.isBlank()) continue;

            List<float[]> vs = (c.getTags() == null ? List.<TagResponse>of() : c.getTags())
                    .stream()
                    .map(TagResponse::getTagName)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .filter(s -> !s.isEmpty())
                    .map(tagVec::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());

            if (!vs.isEmpty()) {
                centroids.put(categoryName, mean(vs));
            }
            // 태그 임베딩이 하나도 없으면 해당 카테고리는 건너뜀
        }

        return centroids;
    }

    // ---- helpers ----

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

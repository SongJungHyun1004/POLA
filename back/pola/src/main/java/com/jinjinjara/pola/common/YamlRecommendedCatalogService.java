package com.jinjinjara.pola.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.jinjinjara.pola.common.dto.RecommendedYaml;
import com.jinjinjara.pola.data.dto.request.CategoryWithTags;
import com.jinjinjara.pola.data.dto.request.InitCategoryTagRequest;
import com.jinjinjara.pola.data.dto.response.RecommendedCategory;
import com.jinjinjara.pola.data.dto.response.RecommendedCategoryList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class YamlRecommendedCatalogService {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    @Value("${pola.recommended.yaml-path:classpath:default-categories.yml}")
    private String yamlPath;

    @Transactional(readOnly = true)
    public RecommendedCategoryList getRecommendedCategories() {
        RecommendedYaml data = loadYaml();
        List<RecommendedCategory> list = data.getCategories().stream()
                .map(c -> new RecommendedCategory(
                        c.getName(),
                        sanitize(c.getTags())
                )).toList();
        return new RecommendedCategoryList(list);
    }

    @Transactional(readOnly = true)
    public InitCategoryTagRequest toInitRequest() {
        RecommendedYaml data = loadYaml();
        List<CategoryWithTags> items = data.getCategories().stream()
                .map(c -> new CategoryWithTags(c.getName(), sanitize(c.getTags())))
                .toList();
        return new InitCategoryTagRequest(items);
    }

    private RecommendedYaml loadYaml() {
        try {
            var resource = resourceLoader.getResource(yamlPath);
            return yaml.readValue(resource.getInputStream(), RecommendedYaml.class);
        } catch (IOException e) {
            throw new CustomException(ErrorCode.INTERNAL_SERVER_ERROR,
                    "추천 카테고리 YAML 로드 실패: " + e.getMessage());
        }
    }

    private List<String> sanitize(List<String> tags) {
        if (tags == null) return List.of();
        return tags.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .map(t -> t.replaceAll("^['\"]|['\"]$", ""))
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}

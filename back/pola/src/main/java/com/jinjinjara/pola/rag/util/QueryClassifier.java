package com.jinjinjara.pola.rag.util;


import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinjinjara.pola.rag.dto.common.QueryType;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@Slf4j
public class QueryClassifier {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    @Value("${nlp.paths.query-tokens:classpath:nlp/query_tokens.yml}")
    private String yamlPath;

    private Map<QueryType, List<String>> queryKeywordMap = new EnumMap<>(QueryType.class);

    public QueryClassifier(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void init() {
        try {
            var res = resourceLoader.getResource(yamlPath);
            Map<String, Object> map = yaml.readValue(res.getInputStream(), new TypeReference<>() {});

            Map<String, List<String>> raw = (Map<String, List<String>>) map.get("queryTypes");
            if (raw != null) {
                for (var e : raw.entrySet()) {
                    try {
                        QueryType type = QueryType.valueOf(e.getKey().toUpperCase());
                        queryKeywordMap.put(type, e.getValue());
                    } catch (IllegalArgumentException ignore) {
                        log.warn("[QueryClassifier] Unknown query type: {}", e.getKey());
                    }
                }
            }
            log.info("[QueryClassifier] loaded types: {}", queryKeywordMap.keySet());
        } catch (Exception e) {
            log.error("[QueryClassifier] failed to load {}", yamlPath, e);
        }
    }

    public QueryType classify(String query) {
        if (query == null || query.isBlank()) return QueryType.QA;

        for (var entry : queryKeywordMap.entrySet()) {
            for (String kw : entry.getValue()) {
                if (query.contains(kw)) return entry.getKey();
            }
        }
        return QueryType.QA;
    }
}
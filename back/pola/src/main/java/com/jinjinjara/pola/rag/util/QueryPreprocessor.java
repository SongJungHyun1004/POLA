package com.jinjinjara.pola.rag.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jinjinjara.pola.rag.dto.common.QueryPreprocessResult;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;

@Component
@Slf4j
public class QueryPreprocessor {

    private final ResourceLoader resourceLoader;
    private final ObjectMapper yaml = new ObjectMapper(new YAMLFactory());

    @Value("${nlp.paths.queryTokens:classpath:nlp/query_tokens.yml}")
    private String queryTokensPath;

    private Set<String> STOPWORDS = Set.of();
    private Map<String, String> TIME_TOKENS = Map.of();

    public QueryPreprocessor(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    private void loadTokens() {
        try {
            var res = resourceLoader.getResource(queryTokensPath);
            if (res.exists()) {
                Map<String, Object> map = yaml.readValue(res.getInputStream(), new TypeReference<>() {});
                STOPWORDS = new HashSet<>((List<String>) map.getOrDefault("stopwords", List.of()));
                TIME_TOKENS = (Map<String, String>) map.getOrDefault("timeTokens", Map.of());
                log.info("[QueryPreprocessor] loaded stopwords={}, timeTokens={} from {}", STOPWORDS.size(), TIME_TOKENS.size(), queryTokensPath);
            } else {
                log.warn("[QueryPreprocessor] resource not found: {}", queryTokensPath);
            }
        } catch (Exception e) {
            log.error("[QueryPreprocessor] failed to load tokens from {}", queryTokensPath, e);
        }
    }

    public QueryPreprocessResult preprocess(String rawQuery) {
        if (rawQuery == null || rawQuery.isBlank()) {
            return new QueryPreprocessResult("", null, null);
        }

        String q = rawQuery.strip();
        LocalDate now = LocalDate.now();
        LocalDate start = null, end = null;

        for (var e : TIME_TOKENS.entrySet()) {
            if (q.contains(e.getKey())) {
                String offset = e.getValue();
                if (offset.endsWith("d")) {
                    int days = Integer.parseInt(offset.replace("d", ""));
                    start = now.plusDays(days);
                    end = start;
                } else if (offset.endsWith("w")) {
                    int weeks = Integer.parseInt(offset.replace("w", ""));
                    start = now.plusWeeks(weeks).with(java.time.DayOfWeek.MONDAY);
                    end = start.plusDays(6);
                } else if (offset.endsWith("m")) {
                    int months = Integer.parseInt(offset.replace("m", ""));
                    start = now.plusMonths(months).withDayOfMonth(1);
                    end = start.plusMonths(1).minusDays(1);
                }
                break;
            }
        }

        for (String stop : STOPWORDS) {
            q = q.replace(stop, "");
        }
        q = q.replaceAll("\\s+", " ").trim();

        log.info("[Preprocessor] '{}' → '{}' (range: {} ~ {})", rawQuery, q, start, end);
        return new QueryPreprocessResult(q, start, end);
    }
}

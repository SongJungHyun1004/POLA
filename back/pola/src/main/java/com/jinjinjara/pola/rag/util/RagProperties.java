package com.jinjinjara.pola.rag.util;

import com.jinjinjara.pola.rag.dto.common.QueryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.validation.annotation.Validated;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "rag")
@Validated
@Getter @Setter
public class RagProperties {

    @Valid
    private Similarity similarity = new Similarity();
    @Valid
    private Context context = new Context();

    @Getter @Setter
    public static class Similarity {
        @DecimalMin("0.0") @DecimalMax("1.0")
        private double min = 0.2;
        @DecimalMin("0.0") @DecimalMax("1.0")
        private double keepRatio = 0.6;
        private Map<QueryType, TypePolicy> perType = new HashMap<>();
    }
    @Getter @Setter public static class TypePolicy {
        private Double min;
        private Double keepRatio;
        private List<Double> backoff; // 없으면 코드에서 기본값 사용
    }
    @Getter @Setter public static class Context {
        private int maxDocs = 6;
        private int maxChars = 3000;
        private Map<QueryType, CtxPolicy> perType = new HashMap<>();
    }
    @Getter @Setter public static class CtxPolicy {
        private Integer maxDocs;
        private Integer maxChars;
    }
}
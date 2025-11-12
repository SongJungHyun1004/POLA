package com.jinjinjara.pola.rag.util;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@RequiredArgsConstructor
@Slf4j
@Configuration
@EnableConfigurationProperties(RagProperties.class)
@PropertySource(value = "classpath:nlp/rag-config.yml", factory = YamlPropertySourceFactory.class)
public class RagConfigLoader {
    private final RagProperties ragProperties;

    @PostConstruct
    void logRag() {
        log.info("✅ RAG 설정 확인: min={}, keepRatio={}",
                ragProperties.getSimilarity().getMin(),
                ragProperties.getSimilarity().getKeepRatio());
    }
}

package com.jinjinjara.pola.rag.util;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Slf4j
@Configuration
@RequiredArgsConstructor
@EnableConfigurationProperties(RagProperties.class)
@PropertySource(
        value = "classpath:nlp/rag-config.yml",
        factory = YamlPropertySourceFactory.class,
        ignoreResourceNotFound = false)
public class RagConfigLoader {

    private final RagProperties ragProperties;

    @PostConstruct
    void logLoadedConfig() {
        var sim = ragProperties.getSimilarity();
        log.info("[RAG] Configuration loaded:");
        log.info("  min={} keepRatio={} backoff={}", sim.getMin(), sim.getKeepRatio(), sim.getBackoff());
        log.info("  perType keys={}", sim.getPerType().keySet());
        log.info("  context defaults: maxDocs={} maxChars={}",
                ragProperties.getContext().getMaxDocs(),
                ragProperties.getContext().getMaxChars());
    }
}

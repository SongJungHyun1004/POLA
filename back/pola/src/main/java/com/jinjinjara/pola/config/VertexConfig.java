package com.jinjinjara.pola.config;

import com.google.genai.Client;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VertexConfig {
    @Bean
    public Client vertexClient() {

        String projectId = getenvOrThrow("GCP_PROJECT_ID");
        String rawLocation = getenvOrDefault("GCP_LOCATION", "us-central1");
        String location = ("global".equalsIgnoreCase(rawLocation)) ? "us-central1" : rawLocation;

        return Client.builder()
                .project(projectId)
                .location(location)
                .vertexAI(true)
                .build();
    }

    private static String getenvOrThrow(String key) {
        String v = System.getenv(key);
        if (v == null || v.isBlank()) {
            throw new IllegalStateException("Missing required env: " + key);
        }
        return v;
    }

    private static String getenvOrDefault(String key, String def) {
        String v = System.getenv(key);
        return (v == null || v.isBlank()) ? def : v;
    }
}

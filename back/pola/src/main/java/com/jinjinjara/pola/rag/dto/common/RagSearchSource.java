package com.jinjinjara.pola.rag.dto.common;

public record RagSearchSource(
        Long id,
        java.net.URL src,
        String context,
        Double relevanceScore
) {}
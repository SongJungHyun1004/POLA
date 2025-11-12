package com.jinjinjara.pola.rag.dto.common;

public interface SearchRow {
    Long getId();
    String getSrc();
    String getContext();
    Double getRelevanceScore();
}

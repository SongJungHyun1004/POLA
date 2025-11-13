package com.jinjinjara.pola.rag.dto.common;


import lombok.Getter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@ToString
public class SearchRow {
    private Long id;
    private String src;
    private String type;
    private String context;
    private Boolean favorite;
    private String ocrText;
    private Double relevanceScore;
    private Timestamp createdAt;

    public SearchRow(
            Long id,
            String src,
            String type,
            Boolean favorite,
            String ocrText,
            String context,
            Double relevanceScore,
            Timestamp createdAt
    ) {
        this.id = id;
        this.src = src;
        this.type = type;
        this.favorite = favorite;
        this.ocrText = ocrText;
        this.context = context;
        this.relevanceScore = relevanceScore;
        this.createdAt = createdAt;
    }
}
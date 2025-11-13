package com.jinjinjara.pola.rag.dto.common;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RagSearchSource {
    private Long id;
    private java.net.URL src;
    private String type;
    private String context;
    private Double relevanceScore;
    private List<String> tags;
    private Boolean favorite;
    @JsonProperty("ocr_text")
    private String ocrText;
    private LocalDateTime createdAt;
}
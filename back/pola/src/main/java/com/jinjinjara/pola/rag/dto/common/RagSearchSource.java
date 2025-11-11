package com.jinjinjara.pola.rag.dto.common;

import lombok.*;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@ToString
public class RagSearchSource {
    private Long id;
    private java.net.URL src;
    private String context;
    private Double relevanceScore;
}
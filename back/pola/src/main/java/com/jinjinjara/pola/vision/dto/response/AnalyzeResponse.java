package com.jinjinjara.pola.vision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AnalyzeResponse {
    private Long categoryId;
    private String categoryName;
    private List<String> tags;
    private String description;
}
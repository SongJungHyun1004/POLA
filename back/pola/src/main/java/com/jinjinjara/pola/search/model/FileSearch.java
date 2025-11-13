package com.jinjinjara.pola.search.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileSearch {
    private Long fileId;
    private Long userId;
    private String categoryName;
    private String tags;
    private String context;
    private String ocrText;
    private String imageUrl;
    private String createdAt;
    private Boolean favorite;
    private String fileType;
}

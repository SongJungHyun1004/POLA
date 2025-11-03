package com.jinjinjara.pola.search.document;


import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Document(indexName = "pola-files-index")
public class FileDocument {

    @Id
    private Long fileId;
    private Long userId;
    private String categoryName;
    private String tags;
    private String context;
    private String ocrText;
    private String imageUrl;
    private LocalDateTime createdAt;
}

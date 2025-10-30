package com.jinjinjara.pola.data.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InsertDataResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("user_id")
    private Long userId;

    @JsonProperty("category_id")
    private Long categoryId;

    @JsonProperty("src")
    private String src;

    @JsonProperty("type")
    private String type;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("context")
    private String context;

    @JsonProperty("text_ocr")
    private String textOcr;

    @JsonProperty("platform")
    private String platform;

    @JsonProperty("origin_url")
    private String originUrl;

}

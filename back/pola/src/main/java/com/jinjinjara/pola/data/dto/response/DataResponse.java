package com.jinjinjara.pola.data.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("src")
    private String src;

    @JsonProperty("type")
    private String type;

    @JsonProperty("context")
    private String context;

    @JsonProperty("favorite")
    private Boolean favorite;

    @JsonProperty("ocr_text")
    private String ocrText;

    @JsonProperty("tags")
    private List<String> tags;

    @JsonProperty("createdAt")
    private LocalDateTime createdAt;


}

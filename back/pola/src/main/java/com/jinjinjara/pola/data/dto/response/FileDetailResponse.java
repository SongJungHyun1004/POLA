package com.jinjinjara.pola.data.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileDetailResponse {

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

    @JsonProperty("context")
    private String context;

    @JsonProperty("ocr_text")
    private String ocrText;

    @JsonProperty("vector_id")
    private Long vectorId;

    @JsonProperty("file_size")
    private Long fileSize;

    @JsonProperty("share_status")
    private Boolean shareStatus;

    @JsonProperty("favorite")
    private Boolean favorite;

    @JsonProperty("favorite_sort")
    private Integer favoriteSort;

    @JsonProperty("favorited_at")
    private LocalDateTime favoritedAt;

    @JsonProperty("views")
    private Integer views;

    @JsonProperty("platform")
    private String platform;

    @JsonProperty("origin_url")
    private String originUrl;

    @JsonProperty("created_at")
    private LocalDateTime createdAt;

    @JsonProperty("last_viewed_at")
    private LocalDateTime lastViewedAt;

    @JsonProperty("tags")
    private List<TagResponse> tags;
}

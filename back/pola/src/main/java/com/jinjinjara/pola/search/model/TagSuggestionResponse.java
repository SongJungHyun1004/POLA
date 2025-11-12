package com.jinjinjara.pola.search.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 태그 자동완성 응답 DTO
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Schema(description = "태그 자동완성 응답")
public class TagSuggestionResponse {

    @Schema(description = "검색된 태그 목록", example = "[\"립\", \"립스틱\", \"립밤\"]")
    private List<String> tags;

    @Schema(description = "검색된 태그 개수", example = "3")
    private int count;

    public static TagSuggestionResponse from(List<String> tags) {
        return TagSuggestionResponse.builder()
                .tags(tags)
                .count(tags.size())
                .build();
    }
}

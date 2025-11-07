package com.jinjinjara.pola.data.dto.response;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithTagsResponse {
    private Long categoryId;
    private String categoryName;
    private List<TagResponse> tags;
}

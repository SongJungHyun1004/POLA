package com.jinjinjara.pola.data.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryWithTags {
    private String categoryName;
    private List<String> tags;
}

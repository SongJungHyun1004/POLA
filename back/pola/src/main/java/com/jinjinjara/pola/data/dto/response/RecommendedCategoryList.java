package com.jinjinjara.pola.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;


@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendedCategoryList {
    private List<RecommendedCategory> recommendations;
}

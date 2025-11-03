package com.jinjinjara.pola.data.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoryDataResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("category_name")
    private String categoryName;

    @JsonProperty("category_sort")
    private Integer categorySort;

    @JsonProperty("category_data")
    private List<DataResponse> data;
}

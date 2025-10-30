package com.jinjinjara.pola.user.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserCategoryResponse {

    @JsonProperty("id")
    private Long id;

    @JsonProperty("tag_name")
    private String tagName;
}

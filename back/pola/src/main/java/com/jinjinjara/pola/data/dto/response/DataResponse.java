package com.jinjinjara.pola.data.dto.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import lombok.Getter;
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

    @JsonProperty("tags")
    private List<String> tags;
}

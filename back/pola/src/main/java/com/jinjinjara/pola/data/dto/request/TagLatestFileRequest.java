package com.jinjinjara.pola.data.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TagLatestFileRequest {

    @Schema(description = "카테고리 ID", example = "5")
    private Long categoryId;
}

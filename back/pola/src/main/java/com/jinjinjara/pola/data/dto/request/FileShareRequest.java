package com.jinjinjara.pola.data.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileShareRequest {

    @Schema(description = "공유 만료 시간 (단위: 시간)", example = "24")
    private Integer expireHours;
}

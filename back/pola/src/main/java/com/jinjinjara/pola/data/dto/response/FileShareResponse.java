package com.jinjinjara.pola.data.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class FileShareResponse {

    @Schema(description = "공유 링크 URL")
    private String shareUrl;

    @Schema(description = "만료 시각 (ISO 포맷)")
    private String expiredAt;
}

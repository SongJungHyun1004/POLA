package com.jinjinjara.pola.report.dto.request;

import com.jinjinjara.pola.report.entity.ReportType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 특정 타입으로 리포트 생성 요청 DTO (테스트용)
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "특정 타입으로 리포트 생성 요청")
public class GenerateWithTypeRequest {

    @NotNull(message = "리포트 타입은 필수입니다.")
    @Schema(
            description = "생성할 리포트 타입",
            example = "SCREENSHOT_MASTER",
            allowableValues = {
                    "TAG_ONE_WELL",
                    "SCREENSHOT_MASTER",
                    "OCTOPUS_COLLECTOR",
                    "TRIPITAKA_MASTER",
                    "NIGHT_OWL",
                    "MIRACLE_MORNING_BEAR",
                    "NO_TYPE"
            }
    )
    private ReportType reportType;
}

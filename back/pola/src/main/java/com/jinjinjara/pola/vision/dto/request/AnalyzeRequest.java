package com.jinjinjara.pola.vision.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzeRequest {
    @NotBlank
    private String s3Url;
}

package com.jinjinjara.pola.vision.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class LabelResponse {
    private String description;
    private float score;
}

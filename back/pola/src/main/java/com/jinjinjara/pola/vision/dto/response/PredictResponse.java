package com.jinjinjara.pola.vision.dto.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.jinjinjara.pola.vision.dto.common.Prediction;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PredictResponse {
    private List<Prediction> predictions;
}

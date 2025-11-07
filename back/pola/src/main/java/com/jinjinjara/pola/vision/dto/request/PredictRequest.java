package com.jinjinjara.pola.vision.dto.request;

import com.jinjinjara.pola.vision.dto.common.Instance;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PredictRequest {
    private List<Instance> instances;
}
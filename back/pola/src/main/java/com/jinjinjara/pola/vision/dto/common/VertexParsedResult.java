package com.jinjinjara.pola.vision.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class VertexParsedResult {
    List<String> tags;
    String description;
}

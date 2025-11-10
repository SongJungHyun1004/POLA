package com.jinjinjara.pola.vision.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Result{
    List<Score> results;
    String topCategory;
    List<InputRel> topCategoryInputs;
}

package com.jinjinjara.pola.vision.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class Score {
    String category;
    double similarity;
    List<Evidence> topTags;
}
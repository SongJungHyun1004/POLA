package com.jinjinjara.pola.vision.dto.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class Score {
    String category;
    double similarity;
    List<Evidence> topTags;
}
package com.jinjinjara.pola.vision.dto.response;

import com.jinjinjara.pola.vision.dto.common.InputRel;
import com.jinjinjara.pola.vision.dto.common.Score;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AnalyzeResponse {

    private List<String> inputTags;

    private String topCategory;

    private List <Score> scores;

    private List<InputRel> topCategoryInputs;

    private String builtAt;
}

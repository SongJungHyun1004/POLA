package com.jinjinjara.pola.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagLatestFileResponse {
    private Long tagId;
    private String tagName;
    private Long fileCount;
    private DataResponse latestFile; // 대표 파일 1개
}

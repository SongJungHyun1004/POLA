package com.jinjinjara.pola.data.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FileUpdateRequest {
    private String context; // 파일 설명 (텍스트만 수정)
}

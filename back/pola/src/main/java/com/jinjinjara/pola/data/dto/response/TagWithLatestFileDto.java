package com.jinjinjara.pola.data.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TagWithLatestFileDto {
    private Long tagId;
    private String tagName;
    private Long fileCount;
    private LocalDateTime latestFileCreatedAt;
}

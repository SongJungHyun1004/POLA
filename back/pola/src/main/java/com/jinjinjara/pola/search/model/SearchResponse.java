package com.jinjinjara.pola.search.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 검색 API 응답 DTO
 * 검색 결과와 총 개수를 함께 반환합니다.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "검색 결과 응답")
public class SearchResponse {

    @Schema(description = "검색된 파일 총 개수", example = "42")
    private int totalCount;

    @Schema(description = "검색 결과 리스트")
    private List<FileSearch> results;

    /**
     * 검색 결과 리스트로부터 SearchResponse 생성
     */
    public static SearchResponse from(List<FileSearch> results) {
        return new SearchResponse(results.size(), results);
    }
}

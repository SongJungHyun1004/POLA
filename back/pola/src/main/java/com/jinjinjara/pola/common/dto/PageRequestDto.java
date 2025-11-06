package com.jinjinjara.pola.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "공통 페이징 및 필터 요청 DTO")
public class PageRequestDto {

    @Schema(description = "페이지 번호 (0부터 시작)", example = "0")
    @Builder.Default
    private int page = 0;

    @Schema(description = "페이지 크기", example = "20")
    @Builder.Default
    private int size = 20;

    @Schema(description = "정렬 기준 필드명", example = "createdAt")
    @Builder.Default
    private String sortBy = "createdAt";

    @Schema(description = "정렬 방향 (ASC/DESC)", example = "DESC")
    @Builder.Default
    private String direction = "DESC";

    @Schema(description = "필터 종류 (category, favorite 등)", example = "category")
    private String filterType;

    @Schema(description = "필터에 사용할 ID (카테고리 ID 등)", example = "5")
    private Long filterId;

    public Pageable toPageable() {
        Sort.Direction dir = direction.equalsIgnoreCase("ASC")
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        return PageRequest.of(page, size, Sort.by(dir, sortBy));
    }
}

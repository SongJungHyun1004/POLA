package com.jinjinjara.pola.common.dto;


import com.jinjinjara.pola.data.entity.File;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileResponseDto {
    private Long id;            // 파일 ID
    private String src;         // 저장된 S3 파일 URL
    private Boolean favorite;   // 즐겨찾기 여부
    private Integer favoriteSort; // 즐겨찾기 순서

    public static FileResponseDto fromEntity(File file) {
        return FileResponseDto.builder()
                .id(file.getId())
                .src(file.getOriginUrl() != null ? file.getOriginUrl() : file.getSrc()) // 원본 URL 우선
                .favorite(file.getFavorite())
                .favoriteSort(file.getFavoriteSort())
                .build();
    }
}

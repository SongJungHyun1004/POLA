package com.jinjinjara.pola.data.dto.response;


import com.jinjinjara.pola.data.entity.FileTag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FileTagResponse {
    private Long id;
    private Long fileId;
    private Long tagId;

    public static FileTagResponse fromEntity(FileTag entity) {
        return new FileTagResponse(
                entity.getId(),
                entity.getFile().getId(),
                entity.getTag().getId()
        );
    }
}

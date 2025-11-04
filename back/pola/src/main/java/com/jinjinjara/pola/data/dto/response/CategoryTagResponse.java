package com.jinjinjara.pola.data.dto.response;


import com.jinjinjara.pola.data.entity.CategoryTag;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryTagResponse {
    private Long id;
    private Long categoryId;
    private Long tagId;

    public static CategoryTagResponse fromEntity(CategoryTag entity) {
        return new CategoryTagResponse(
                entity.getId(),
                entity.getCategory().getId(),
                entity.getTag().getId()
        );
    }
}

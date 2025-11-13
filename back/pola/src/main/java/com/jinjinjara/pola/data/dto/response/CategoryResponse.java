package com.jinjinjara.pola.data.dto.response;


import com.jinjinjara.pola.data.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CategoryResponse {
    private Long id;
    private String categoryName;
    private Integer categorySort;
    private LocalDateTime createdAt;
    private String userEmail;

    public static CategoryResponse fromEntity(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getCategoryName(),
                category.getFileCount(),
                category.getCreatedAt(),
                category.getUser().getEmail()
        );
    }
}

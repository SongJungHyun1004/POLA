package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.data.dto.response.CategoryResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // CREATE
    public CategoryResponse createCategory(Users user, String categoryName) {
        Category category = Category.builder()
                .user(user)
                .categoryName(categoryName)
                .categorySort(0)
                .build();

        Category saved = categoryRepository.save(category);
        return CategoryResponse.fromEntity(saved);
    }

    // READ - 전체 조회 (유저 기준)
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByUser(Users user) {
        return categoryRepository.findByUser(user)
                .stream()
                .map(CategoryResponse::fromEntity)
                .toList();
    }

    // READ - 단일 조회
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));
        return CategoryResponse.fromEntity(category);
    }

    // UPDATE
    public CategoryResponse updateCategory(Long id, String newName) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Category not found: " + id));

        category = Category.builder()
                .id(category.getId())
                .user(category.getUser())
                .categoryName(newName)
                .categorySort(category.getCategorySort())
                .createdAt(category.getCreatedAt())
                .build();

        Category updated = categoryRepository.save(category);
        return CategoryResponse.fromEntity(updated);
    }

    // DELETE
    public void deleteCategory(Long id) {
        categoryRepository.deleteById(id);
    }
}

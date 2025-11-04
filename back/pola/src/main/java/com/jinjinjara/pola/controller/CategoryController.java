package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.response.CategoryResponse;
import com.jinjinjara.pola.data.service.CategoryService;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users/me/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    // 임시 유저 (추후 인증 연동 시 대체)
    private Users mockUser() {
        return Users.builder().id(1L).build();
    }

    // CREATE (카테고리 생성)
    @PostMapping
    public ApiResponse<CategoryResponse> create(@RequestParam String name) {
        try {
            CategoryResponse category = categoryService.createCategory(mockUser(), name);
            return ApiResponse.ok(category, "카테고리가 성공적으로 생성되었습니다.");
        } catch (Exception e) {
            return ApiResponse.fail("CATEGORY_CREATE_FAIL", e.getMessage());
        }
    }

    // READ ALL (카테고리 전체 조회)
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAll() {
        try {
            List<CategoryResponse> categories = categoryService.getCategoriesByUser(mockUser());
            return ApiResponse.ok(categories, "카테고리 목록 조회 완료");
        } catch (Exception e) {
            return ApiResponse.fail("CATEGORY_LIST_FAIL", e.getMessage());
        }
    }

    // READ ONE (단일 카테고리 조회)
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getById(@PathVariable Long id) {
        try {
            CategoryResponse category = categoryService.getCategoryById(id);
            return ApiResponse.ok(category, "카테고리 조회 완료");
        } catch (Exception e) {
            return ApiResponse.fail("CATEGORY_NOT_FOUND", e.getMessage());
        }
    }

    // UPDATE (카테고리 이름 수정)
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @RequestParam String name) {
        try {
            CategoryResponse updated = categoryService.updateCategory(id, name);
            return ApiResponse.ok(updated, "카테고리 수정 완료");
        } catch (Exception e) {
            return ApiResponse.fail("CATEGORY_UPDATE_FAIL", e.getMessage());
        }
    }

    // DELETE (카테고리 삭제)
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ApiResponse.ok(null, "카테고리 삭제 완료");
        } catch (Exception e) {
            return ApiResponse.fail("CATEGORY_DELETE_FAIL", e.getMessage());
        }
    }
}

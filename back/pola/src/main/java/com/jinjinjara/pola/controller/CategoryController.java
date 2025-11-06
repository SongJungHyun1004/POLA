package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.dto.response.CategoryResponse;
import com.jinjinjara.pola.data.service.CategoryService;
import com.jinjinjara.pola.user.entity.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/v1/users/me/categories")
@RequiredArgsConstructor
@Tag(name = "Category API", description = "유저별 카테고리 관리 API (생성, 조회, 수정, 삭제)")
public class CategoryController {

    private final CategoryService categoryService;


    @Operation(summary = "카테고리 생성")
    @PostMapping
    public ApiResponse<CategoryResponse> create(@RequestParam String name
    ,@AuthenticationPrincipal Users user) {
        CategoryResponse category = categoryService.createCategory(user, name);
        return ApiResponse.ok(category, "카테고리가 성공적으로 생성되었습니다.");
    }

    @Operation(summary = "카테고리 전체 조회")
    @GetMapping
    public ApiResponse<List<CategoryResponse>> getAll(@AuthenticationPrincipal Users user) {
        List<CategoryResponse> categories = categoryService.getCategoriesByUser(user);
        return ApiResponse.ok(categories, "카테고리 목록 조회 완료");
    }

    @Operation(summary = "단일 카테고리 조회")
    @GetMapping("/{id}")
    public ApiResponse<CategoryResponse> getById(@PathVariable Long id) {
        CategoryResponse category = categoryService.getCategoryById(id);
        return ApiResponse.ok(category, "카테고리 조회 완료");
    }

    @Operation(summary = "카테고리 수정")
    @PutMapping("/{id}")
    public ApiResponse<CategoryResponse> update(@PathVariable Long id, @RequestParam String name) {
        CategoryResponse updated = categoryService.updateCategory(id, name);
        return ApiResponse.ok(updated, "카테고리 수정 완료");
    }

    @Operation(summary = "카테고리 삭제")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        categoryService.deleteCategory(id);
        return ApiResponse.ok(null, "카테고리 삭제 완료");
    }
}

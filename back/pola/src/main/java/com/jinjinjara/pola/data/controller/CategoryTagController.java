package com.jinjinjara.pola.data.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.request.InitCategoryTagRequest;
import com.jinjinjara.pola.data.dto.response.CategoryTagResponse;
import com.jinjinjara.pola.data.dto.response.CategoryWithTagsResponse;
import com.jinjinjara.pola.data.dto.response.RecommendedCategoryList;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.service.CategoryTagService;
import com.jinjinjara.pola.user.entity.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "CategoryTag API", description = "카테고리-태그 연결 관리 API (추가, 삭제, 조회, 태그 관리)")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class CategoryTagController {

    private final CategoryTagService categoryTagService;
    @Operation(summary = "카테고리에 태그 추가", description = "특정 카테고리에 선택한 태그를 연결합니다.")
    @PostMapping("/categories/{categoryId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<CategoryTagResponse>> addTagToCategory(
            @Parameter(description = "카테고리 ID", example = "1") @PathVariable Long categoryId,
            @Parameter(description = "태그 ID", example = "5") @PathVariable Long tagId
    ) {
        CategoryTagResponse response = categoryTagService.addTagToCategory(categoryId, tagId);
        return ResponseEntity.ok(ApiResponse.ok(response, "κα테고리에 태그가 추가되었습니다."));
    }

    @Operation(summary = "카테고리에서 태그 제거", description = "특정 카테고리에서 지정된 태그를 제거합니다.")
    @DeleteMapping("/categories/{categoryId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> removeTagFromCategory(
            @Parameter(description = "카테고리 ID", example = "1") @PathVariable Long categoryId,
            @Parameter(description = "태그 ID", example = "5") @PathVariable Long tagId
    ) {
        categoryTagService.removeTagFromCategory(categoryId, tagId);
        return ResponseEntity.ok(ApiResponse.ok(null, "카테고리에서 태그가 제거되었습니다."));
    }

    @Operation(summary = "카테고리 내 태그 조회", description = "특정 카테고리에 연결된 모든 태그를 조회합니다.")
    @GetMapping("/categories/{categoryId}/tags")
    public ApiResponse<List<TagResponse>> getTagsByCategory(@PathVariable Long categoryId) {
        List<TagResponse> tags = categoryTagService.getTagsByCategory(categoryId);
        if (tags == null || tags.isEmpty()) {
            return ApiResponse.ok(null, "카테고리 태그가 없습니다.");
        }
        return ApiResponse.ok(tags, "카테고리 태그 목록 조회 완료");
    }


    @Operation(summary = "전체 태그 조회", description = "등록된 모든 태그를 조회합니다.")
    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        List<TagResponse> tags = categoryTagService.getAllTags();
        return ResponseEntity.ok(ApiResponse.ok(tags, "전체 태그 목록 조회 완료"));
    }

    @Operation(summary = "새 태그 생성", description = "새로운 태그를 생성합니다.")
    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(
            @Parameter(description = "생성할 태그 이름", example = "인공지능") @RequestParam String name
    ) {
        TagResponse tag = categoryTagService.createTag(name);
        return ResponseEntity.ok(ApiResponse.ok(tag, "새 태그가 생성되었습니다."));
    }

    @Operation(summary = "태그 삭제", description = "특정 태그를 삭제합니다.")
    @DeleteMapping("/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(
            @Parameter(description = "삭제할 태그 ID", example = "7") @PathVariable Long tagId
    ) {
        categoryTagService.deleteTag(tagId);
        return ResponseEntity.ok(ApiResponse.ok(null, "태그 삭제 완료"));
    }


    @Operation(summary = "추천 카테고리/태그 목록", description = "기본 카테고리와 추천 태그 리스트를 반환합니다.")
    @GetMapping("/categories/tags/recommendations")
    public ResponseEntity<ApiResponse<RecommendedCategoryList>> getRecommendations() {
        RecommendedCategoryList data = categoryTagService.getRecommendedCategoriesAndTags();
        return ResponseEntity.ok(ApiResponse.ok(data, "추천 카테고리/태그 조회 성공"));
    }

    @Operation(summary = "카테고리/태그 초기 등록", description = "사용자 선택 카테고리/태그를 등록하며 '미분류'를 자동 추가합니다.")
    @PostMapping("/categories/tags/init")
    public ResponseEntity<ApiResponse<Void>> initCategoriesAndTags(@RequestBody InitCategoryTagRequest request
    ,@AuthenticationPrincipal Users user) {
        categoryTagService.initCategoriesAndTags(user, request);
        return ResponseEntity.ok(ApiResponse.ok(null, "사용자 카테고리/태그 초기화 완료"));
    }
    @Operation(summary = "사용자 전체 카테고리별 태그 조회", description = "유저가 가진 모든 카테고리와 각 카테고리에 연결된 태그들을 반환합니다.")
    @GetMapping("/users/me/categories/tags")
    public ApiResponse<List<CategoryWithTagsResponse>> getUserCategoriesWithTags(
            @AuthenticationPrincipal Users user
    ) {
        List<CategoryWithTagsResponse> response = categoryTagService.getUserCategoriesWithTags(user);
        return ApiResponse.ok(response, "사용자 카테고리별 태그 목록 조회 성공");
    }



}

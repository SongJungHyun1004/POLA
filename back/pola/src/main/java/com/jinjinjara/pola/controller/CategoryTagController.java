package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.response.CategoryTagResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.service.CategoryTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class CategoryTagController {

    private final CategoryTagService categoryTagService;

    @PostMapping("/categories/{categoryId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<CategoryTagResponse>> addTagToCategory(
            @PathVariable Long categoryId, @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(categoryTagService.addTagToCategory(categoryId, tagId)));
    }

    @DeleteMapping("/categories/{categoryId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> removeTagFromCategory(
            @PathVariable Long categoryId, @PathVariable Long tagId
    ) {
        categoryTagService.removeTagFromCategory(categoryId, tagId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/categories/{categoryId}/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTagsByCategory(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ApiResponse.ok(categoryTagService.getTagsByCategory(categoryId)));
    }

    @GetMapping("/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getAllTags() {
        return ResponseEntity.ok(ApiResponse.ok(categoryTagService.getAllTags()));
    }

    @PostMapping("/tags")
    public ResponseEntity<ApiResponse<TagResponse>> createTag(@RequestParam String name) {
        return ResponseEntity.ok(ApiResponse.ok(categoryTagService.createTag(name)));
    }

    @DeleteMapping("/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> deleteTag(@PathVariable Long tagId) {
        categoryTagService.deleteTag(tagId);
        return ResponseEntity.ok(ApiResponse.success());
    }
}

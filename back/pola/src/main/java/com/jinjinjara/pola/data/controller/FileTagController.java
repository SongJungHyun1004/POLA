package com.jinjinjara.pola.data.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.request.AddTagNamesRequest;
import com.jinjinjara.pola.data.dto.response.FileTagResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.dto.response.TagLatestFileResponse;
import com.jinjinjara.pola.data.service.FileTagService;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileTagController {

    private final FileTagService fileTagService;

    @PostMapping("/files/{fileId}/tags")
    public ResponseEntity<ApiResponse<List<FileTagResponse>>> addTagsToFile(
            @PathVariable Long fileId,
            @RequestBody AddTagNamesRequest request,
            @AuthenticationPrincipal Users user
    ) {
        if (request.getTagNames() == null || request.getTagNames().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.fail("INVALID_REQUEST", "추가할 태그 이름이 없습니다."));
        }

        List<FileTagResponse> responses = fileTagService.addTagsToFile(fileId, request.getTagNames(), user);
        return ResponseEntity.ok(ApiResponse.ok(responses, "파일에 태그가 추가되었습니다."));
    }

    @DeleteMapping("/files/{fileId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> removeTagFromFile(
            @PathVariable Long fileId,
            @PathVariable Long tagId,
            @AuthenticationPrincipal Users user
    ) {
        fileTagService.removeTagFromFile(fileId, tagId, user);
        return ResponseEntity.ok(ApiResponse.ok(null, "파일에서 태그가 제거되었습니다."));
    }

    @GetMapping("/files/{fileId}/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTagsByFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal Users user
    ) {
        List<TagResponse> tags = fileTagService.getTagsByFile(fileId, user);
        return ResponseEntity.ok(ApiResponse.ok(tags, "파일에 연결된 태그 목록 조회 완료"));
    }

    @GetMapping("/categories/{categoryId}/tags/latest-files")
    public ResponseEntity<ApiResponse<List<TagLatestFileResponse>>> getTagsWithLatestFiles(
            @PathVariable Long categoryId,
            @AuthenticationPrincipal Users user
    ) {
        List<TagLatestFileResponse> result = fileTagService.getTagsWithLatestFiles(categoryId, user);
        return ResponseEntity.ok(ApiResponse.ok(result, "카테고리 내 태그별 최신 파일 목록 조회 완료"));
    }
}

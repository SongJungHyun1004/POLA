package com.jinjinjara.pola.data.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.request.AddTagsRequest;
import com.jinjinjara.pola.data.dto.response.FileTagResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.service.FileTagService;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/file-tags")
@RequiredArgsConstructor
public class TagController {

    private final FileTagService fileTagService;

    @PostMapping("/{fileId}/tags")
    public ApiResponse<List<FileTagResponse>> addTagsToFile(
            @PathVariable Long fileId,
            @RequestBody AddTagsRequest request,
            @AuthenticationPrincipal Users user
    ) {
        if (request.tagIds() == null || request.tagIds().isEmpty()) {
            return ApiResponse.fail("INVALID_REQUEST", "추가할 태그 ID가 없습니다.");
        }

        List<FileTagResponse> responses = request.tagIds().stream()
                .map(tagId -> fileTagService.addTagToFile(fileId, tagId, user))
                .toList();

        return ApiResponse.ok(responses, "태그가 성공적으로 추가되었습니다.");
    }

    @GetMapping("/{fileId}/tags")
    public ApiResponse<List<TagResponse>> getTagsByFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal Users user
    ) {
        List<TagResponse> tags = fileTagService.getTagsByFile(fileId, user);
        return ApiResponse.ok(tags, "파일 태그 목록 조회 성공");
    }

    @DeleteMapping("/{fileId}/tags/{tagId}")
    public ApiResponse<Void> deleteTagFromFile(
            @PathVariable Long fileId,
            @PathVariable Long tagId,
            @AuthenticationPrincipal Users user
    ) {
        fileTagService.removeTagFromFile(fileId, tagId, user);
        return ApiResponse.success("태그가 성공적으로 삭제되었습니다.");
    }
}

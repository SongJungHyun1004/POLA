package com.jinjinjara.pola.data.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.request.AddTagsRequest;
import com.jinjinjara.pola.data.dto.response.FileTagResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.service.FileTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Tag API", description = "파일에 태그를 추가/조회/삭제하는 API")
@RestController
@RequestMapping("/api/v1/file-tags")
@RequiredArgsConstructor
public class TagController {

    private final FileTagService fileTagService;

    /**
     * 파일에 태그 추가 (POST)
     */
    @Operation(summary = "파일 태그 추가", description = "해당 파일에 사용자가 원하는 태그를 여러 개 추가합니다.")
    @PostMapping("/{fileId}/tags")
    public ApiResponse<List<FileTagResponse>> addTagsToFile(
            @PathVariable("fileId") Long fileId,
            @RequestBody AddTagsRequest request
    ) {
        if (request.tagIds() == null || request.tagIds().isEmpty()) {
            return ApiResponse.fail("INVALID_REQUEST", "추가할 태그 ID가 없습니다.");
        }

        // 여러 개 처리
        List<FileTagResponse> responses = request.tagIds().stream()
                .map(tagId -> fileTagService.addTagToFile(fileId, tagId))
                .toList();

        return ApiResponse.ok(responses, "태그가 성공적으로 추가되었습니다.");
    }


    /**
     * 파일의 태그 목록 조회 (GET)
     */
    @Operation(summary = "파일 태그 목록 조회", description = "해당 파일에 연결된 모든 태그를 반환합니다.")
    @GetMapping("/{fileId}/tags")
    public ApiResponse<List<TagResponse>> getTagsByFile(
            @PathVariable("fileId") Long fileId
    ) {
        List<TagResponse> tags = fileTagService.getTagsByFile(fileId);
        return ApiResponse.ok(tags, "파일 태그 목록 조회에 성공했습니다.");
    }

    /**
     * 파일에서 특정 태그 삭제 (DELETE)
     */
    @Operation(summary = "파일 태그 삭제", description = "해당 파일에서 특정 태그를 제거합니다.")
    @DeleteMapping("/{fileId}/tags/{tagId}")
    public ApiResponse<Void> deleteTagFromFile(
            @PathVariable("fileId") Long fileId,
            @PathVariable("tagId") Long tagId
    ) {
        fileTagService.removeTagFromFile(fileId, tagId);
        return ApiResponse.success("태그가 성공적으로 삭제되었습니다.");
    }
}

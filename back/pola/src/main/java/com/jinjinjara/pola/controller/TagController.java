package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Tag(name = "Tag API", description = "태그 API")
@RestController
@RequestMapping("/api/v1/files")
@RequiredArgsConstructor
public class TagController {

    @Operation(summary = "파일 태그 추가", description = "해당 파일에 사용자가 원하는 태그를 추가합니다.")
    @DeleteMapping("/{id}/tags")
    public ApiResponse<List<TagResponse>> insertTag(
            @PathVariable("id") Long fileId
    ) {
        List<TagResponse> tags = new ArrayList<>();
        tags.add(new TagResponse(10L, "#기존태그"));
        tags.add(new TagResponse(11L, "#새로운태그"));
        return ApiResponse.ok(tags,"태그가 성공적으로 추가되었습니다.");
    }

    @Operation(summary = "파일 태그 추가", description = "해당 파일의 태그를 반환합니다.")
    @GetMapping("/{id}/tags")
    public ApiResponse<List<TagResponse>> getTag(
            @PathVariable("id") Long fileId
    ) {
        List<TagResponse> tags = new ArrayList<>();
        tags.add(new TagResponse(10L, "#기존태그"));
        tags.add(new TagResponse(11L, "#새로운태그"));
        return ApiResponse.ok(tags,"파일 태그 목록 조회에 성공했습니다.");
    }

    @Operation(summary = "파일 태그 삭제", description = "해당 파일에 사용자가 직접 태그를 삭제합니다.")
    @DeleteMapping("/{fileId}/tags/{tagId}")
    public ApiResponse<List<Object>> deleteTag(
            @PathVariable("fileId") Long fileId,
            @PathVariable("tagId") Long tagId
    ) {
        return ApiResponse.okMessage("태그가 성공적으로 삭제되었습니다.");
    }
}

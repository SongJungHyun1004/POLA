package com.jinjinjara.pola.data.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.response.FileTagResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.service.FileTagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "FileTag API", description = "파일-태그 연결 관리 API (추가, 삭제, 조회)")
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class FileTagController {

    private final FileTagService fileTagService;

    @Operation(summary = "파일에 태그 추가", description = "특정 파일에 선택한 태그를 연결합니다.")
    @PostMapping("/files/{fileId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<FileTagResponse>> addTagToFile(
            @Parameter(description = "파일 ID", example = "1") @PathVariable Long fileId,
            @Parameter(description = "태그 ID", example = "3") @PathVariable Long tagId
    ) {
        FileTagResponse response = fileTagService.addTagToFile(fileId, tagId);
        return ResponseEntity.ok(ApiResponse.ok(response, "파일에 태그가 추가되었습니다."));
    }

    @Operation(summary = "파일에서 태그 제거", description = "특정 파일에서 지정된 태그를 제거합니다.")
    @DeleteMapping("/files/{fileId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> removeTagFromFile(
            @Parameter(description = "파일 ID", example = "1") @PathVariable Long fileId,
            @Parameter(description = "태그 ID", example = "3") @PathVariable Long tagId
    ) {
        fileTagService.removeTagFromFile(fileId, tagId);
        return ResponseEntity.ok(ApiResponse.ok(null, "파일에서 태그가 제거되었습니다."));
    }

    @Operation(summary = "파일에 연결된 태그 조회", description = "특정 파일에 연결된 모든 태그를 조회합니다.")
    @GetMapping("/files/{fileId}/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTagsByFile(
            @Parameter(description = "파일 ID", example = "1") @PathVariable Long fileId
    ) {
        List<TagResponse> tags = fileTagService.getTagsByFile(fileId);
        return ResponseEntity.ok(ApiResponse.ok(tags, "파일에 연결된 태그 목록 조회 완료"));
    }
}

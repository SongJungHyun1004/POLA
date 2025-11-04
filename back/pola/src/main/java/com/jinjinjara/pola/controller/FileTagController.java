package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.response.FileTagResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.service.FileTagService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FileTagController {

    private final FileTagService fileTagService;

    @PostMapping("/files/{fileId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<FileTagResponse>> addTagToFile(
            @PathVariable Long fileId, @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(ApiResponse.ok(fileTagService.addTagToFile(fileId, tagId)));
    }

    @DeleteMapping("/files/{fileId}/tags/{tagId}")
    public ResponseEntity<ApiResponse<Void>> removeTagFromFile(
            @PathVariable Long fileId, @PathVariable Long tagId
    ) {
        fileTagService.removeTagFromFile(fileId, tagId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/files/{fileId}/tags")
    public ResponseEntity<ApiResponse<List<TagResponse>>> getTagsByFile(@PathVariable Long fileId) {
        return ResponseEntity.ok(ApiResponse.ok(fileTagService.getTagsByFile(fileId)));
    }
}

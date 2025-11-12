package com.jinjinjara.pola.s3.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;
    private final FileRepository fileRepository;

    @GetMapping("/{fileId}/preview-test")
    @Operation(summary = "미리보기 Presigned URL 테스트", description = "미리보기용 S3 URL을 생성합니다.")
    public ApiResponse<String> testPreview(@PathVariable Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        String url = s3Service.generatePreviewUrl(file.getSrc(), file.getType()).toString();
        return ApiResponse.ok(url, "미리보기 URL 생성 성공");
    }

    @GetMapping("/{fileId}/original-test")
    @Operation(summary = "원본 Presigned URL 테스트", description = "원본 파일용 S3 URL을 생성합니다.")
    public ApiResponse<String> testOriginal(@PathVariable Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        String url = s3Service.generateOriginalPreviewUrl(file.getSrc(), file.getType());
        return ApiResponse.ok(url, "원본 URL 생성 성공");
    }
}

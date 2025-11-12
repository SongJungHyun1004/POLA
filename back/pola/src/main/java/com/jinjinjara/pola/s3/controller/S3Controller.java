package com.jinjinjara.pola.s3.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.s3.dto.response.S3PresignedUrlResponse;
import com.jinjinjara.pola.s3.service.S3Service;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

@RestController
@RequestMapping("/api/v1/s3")
@RequiredArgsConstructor
public class S3Controller {

    private final S3Service s3Service;
    private final FileRepository fileRepository;

    // S3 업로드 Presigned URL 생성
    @GetMapping("/presigned/upload")
    @Operation(summary = "S3 업로드 URL 생성", description = "파일명을 입력받아 S3 업로드용 Presigned URL을 생성합니다. " +
            "헤더에 content-type 을 넣어야 하며, 예시: image/png, image/jpeg, text/plain; charset=utf-8")
    public ApiResponse<S3PresignedUrlResponse> getUploadUrl(
            @Parameter(description = "업로드할 원본 파일 이름", example = "example.png")
            @RequestParam String fileName
    ) {
        S3PresignedUrlResponse response = s3Service.generateUploadUrl(fileName);
        return ApiResponse.ok(response, "S3 업로드 URL 생성 완료");
    }

    //  미리보기 URL 테스트
    @GetMapping("/{fileId}/preview-test")
    @Operation(summary = "미리보기 Presigned URL 테스트", description = "미리보기용 S3 URL을 생성합니다.")
    public ApiResponse<String> testPreview(@PathVariable Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        String url = s3Service.generatePreviewUrl(file.getSrc(), file.getType()).toString();
        return ApiResponse.ok(url, "미리보기 URL 생성 성공");
    }

    //  원본 URL 테스트
    @GetMapping("/{fileId}/original-test")
    @Operation(summary = "원본 Presigned URL 테스트", description = "원본 파일용 S3 URL을 생성합니다.")
    public ApiResponse<String> testOriginal(@PathVariable Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        String url = s3Service.generateOriginalPreviewUrl(file.getSrc(), file.getType());
        return ApiResponse.ok(url, "원본 URL 생성 성공");
    }

    //  다운로드 URL 생성
    @GetMapping("/download/{fileId}")
    @Operation(summary = "S3 다운로드 URL 생성", description = "파일 ID를 입력받아 다운로드용 Presigned URL을 생성합니다.")
    public ApiResponse<String> getDownloadUrl(@PathVariable Long fileId) {
        URL url = s3Service.generateDownloadUrlByFileId(fileId);
        return ApiResponse.ok(url.toString(), "다운로드 URL 생성 완료");
    }
}

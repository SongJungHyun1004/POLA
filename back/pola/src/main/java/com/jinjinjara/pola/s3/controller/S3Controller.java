package com.jinjinjara.pola.s3.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.s3.dto.response.S3PresignedUrlResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URL;

@Tag(name = "S3 API", description = "AWS S3 Presigned URL 발급 API (업로드 / 다운로드)")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files")
public class S3Controller {

    private final S3Service s3Service;

    @Operation(summary = "S3 업로드 URL 생성", description = "파일명을 입력받아 S3 업로드용 Presigned URL을 생성합니다." +
            "헤더에 content-type 을 넣어야하고 타입에 따라서 아래처럼 넣으면됨" +
            "\"text/plain; charset=utf-8\"\n(이거안넣으면 인코딩이상한걸로댐)" +
            "image/png\n" +
            "image/jpg")

    @GetMapping("/s3/presigned/upload")
    public ApiResponse<S3PresignedUrlResponse> getUploadUrl(
            @Parameter(description = "업로드할 원본 파일 이름", example = "example.png")
            @RequestParam String fileName
    ) {
        S3PresignedUrlResponse response = s3Service.generateUploadUrl(fileName);
        return ApiResponse.ok(response, "S3 업로드 URL 생성 완료");
    }

    @Operation(summary = "S3 다운로드 URL 생성", description = "파일 키(key)를 입력받아 S3 다운로드용 Presigned URL을 생성합니다.")
    @GetMapping("/s3/presigned/download")
    public ApiResponse<S3PresignedUrlResponse> getDownloadUrl(
            @Parameter(description = "S3에 저장된 파일 키 (예: home/uuid.png)", example = "home/123e4567-e89b-12d3-a456-426614174000.png")
            @RequestParam String key
    ) {
        URL presignedUrl = s3Service.generateDownloadUrl(key);
        S3PresignedUrlResponse response = new S3PresignedUrlResponse(presignedUrl, key);
        return ApiResponse.ok(response, "S3 다운로드 URL 생성 완료");
    }
}

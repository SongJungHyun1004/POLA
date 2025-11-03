package com.jinjinjara.pola.controller;


import com.jinjinjara.pola.s3.S3Service;
import com.jinjinjara.pola.s3.dto.response.S3PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/files") // API의 기본 경로를 /api/files로 설정
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/s3/presigned/upload")
    public S3PresignedUrlResponse getUploadUrl(@RequestParam String fileName) {
        return s3Service.generateUploadUrl(fileName);
    }

    @GetMapping("/s3/presigned/download")
    public S3PresignedUrlResponse getDownloadUrl(@RequestParam String key) {
        URL presignedUrl = s3Service.generateDownloadUrl(key);
        return new S3PresignedUrlResponse(presignedUrl, key);
    }
}
package com.jinjinjara.pola.controller;


import com.jinjinjara.pola.s3.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.net.URL;
import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/files") // API의 기본 경로를 /api/files로 설정
public class S3Controller {

    private final S3Service s3Service;

    @GetMapping("/s3/presigned/upload")
    public Map<String, String> getUploadUrl(@RequestParam String fileName) {
        URL presignedUrl = s3Service.generateUploadUrl(fileName).getUrl();
        return Map.of("url", presignedUrl.toString());
    }

    @GetMapping("/s3/presigned/download")
    public Map<String, String> getDownloadUrl(@RequestParam String fileName) {
        URL presignedUrl = s3Service.generateDownloadUrl(fileName);
        return Map.of("url", presignedUrl.toString());
    }
}
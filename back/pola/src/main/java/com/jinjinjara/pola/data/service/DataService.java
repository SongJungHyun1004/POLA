package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.data.dto.common.Platform;
import com.jinjinjara.pola.data.dto.response.InsertDataResponse;
import lombok.RequiredArgsConstructor;
import org.joda.time.Instant;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DataService {

    public InsertDataResponse insertData(MultipartFile file, String originUrl, Platform platform) {
        if (file == null || file.isEmpty()) {
//            throw new FileProcessException("파일이 비어 있습니다."); // 커스텀 예외
        }

        // MIME 타입 확인
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String s3Url = "https://s3-bucket/path/to/" + file.getOriginalFilename(); // 목업

        // AI 처리 결과 (목업)
        String ocrText = "아이디\n비밀번호\n로그인";
        String context = "파란색 버튼이 있는 로그인 화면";

        return InsertDataResponse.builder()
                .id(101L)
                .userId(1L)
                .categoryId(5L)
                .src(s3Url)
                .type(contentType)
                .createdAt(LocalDateTime.parse("2025-10-27T10:00:00"))
                .context(context)
                .textOcr(ocrText)
                .platform(platform.name())
                .originUrl(originUrl)
                .build();
    }
}

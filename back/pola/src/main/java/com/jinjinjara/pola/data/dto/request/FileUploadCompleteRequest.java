package com.jinjinjara.pola.data.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FileUploadCompleteRequest {
    private String key;        // S3에 저장된 경로 (ex: home/uuid.jpg)
    private String type;       // 파일 타입 (image, pdf 등)
    private int fileSize;      // 파일 크기 (byte 단위)
    private String originUrl;  // presigned URL의 base URL (S3 접근 링크)
    private String platform;
}

package com.jinjinjara.pola.s3.dto.response;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.net.URL;

@Data
@AllArgsConstructor
public class S3PresignedUrlResponse {
    private URL url;   // 실제로 PUT 요청을 보낼 URL
    private String key; // S3에 저장될 경로 (DB에 저장용)
}

package com.jinjinjara.pola.share.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShareFileResponse {

    @Schema(description = "파일 ID")
    private Long fileId;

    @Schema(description = "Presigned 미리보기 URL")
    private String presignedUrl;

    @Schema(description = "Presigned 다운로드 URL")
    private String downloadUrl;

    @Schema(description = "파일 MIME 타입")
    private String type;

    @Schema(description = "파일 설명 (LLM 컨텍스트)")
    private String context;

    @Schema(description = "OCR 인식 결과")
    private String ocrText;

    @Schema(description = "파일 크기 (Byte)")
    private Long fileSize;

    @Schema(description = "업로드 플랫폼 (웹, 앱, S3 등)")
    private String platform;

    @Schema(description = "원본 URL")
    private String originUrl;

    @Schema(description = "파일 생성 시각")
    private LocalDateTime createdAt;

    @Schema(description = "파일 소유자 이름")
    private String ownerName;

    @Schema(description = "태그 목록")
    private List<String> tags;
}

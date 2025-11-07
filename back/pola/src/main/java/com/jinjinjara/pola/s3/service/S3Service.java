package com.jinjinjara.pola.s3.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.s3.dto.response.S3PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;

import java.net.URL;
import java.time.Duration;
import java.util.*;

/**
 * AWS S3 Presigned URL 관리 서비스
 * - 업로드용, 다운로드용, 미리보기용 URL 생성
 * - 여러 파일을 한 번에 처리 가능
 */
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    /* ===========================================================
        Presigned URL (UPLOAD)
       =========================================================== */

    /**
     * 업로드용 Presigned URL 생성
     * 예: 클라이언트가 S3에 직접 업로드할 때 사용
     */
    public S3PresignedUrlResponse generateUploadUrl(String originalFileName) {
        try {
            String key = buildS3Key(originalFileName);

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            PresignedPutObjectRequest presignedPutObjectRequest = s3Presigner.presignPutObject(builder ->
                    builder.signatureDuration(Duration.ofMinutes(10))
                            .putObjectRequest(objectRequest));

            return new S3PresignedUrlResponse(
                    presignedPutObjectRequest.url(),
                    key
            );
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAIL, e.getMessage());
        }
    }

    /* ===========================================================
        Presigned URL (DOWNLOAD)
       =========================================================== */

    /**
     * 다운로드용 Presigned URL 생성
     */
    public URL generateDownloadUrl(String key) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder ->
                    builder.signatureDuration(Duration.ofMinutes(10))
                            .getObjectRequest(getRequest));

            return presignedRequest.url();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    /* ===========================================================
        Presigned URL (PREVIEW)
       =========================================================== */

    /**
     * 단일 파일 미리보기용 Presigned URL 생성
     * → 브라우저/앱에서 바로 열 수 있음 (inline)
     */
    public URL generatePreviewUrl(String key, String contentType) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .responseContentType(resolveContentType(contentType))
                    .responseContentDisposition("inline") // 다운로드 대신 브라우저 미리보기
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder ->
                    builder.signatureDuration(Duration.ofMinutes(10))
                            .getObjectRequest(getRequest));

            return presignedRequest.url();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * 여러 파일용 미리보기 Presigned URL 생성
     * → key, contentType 함께 받아서 presigned URL 일괄 생성
     */
    public Map<Long, String> generatePreviewUrls(Map<Long, FileMeta> fileMetaMap) {
        Map<Long, String> result = new HashMap<>();

        for (Map.Entry<Long, FileMeta> entry : fileMetaMap.entrySet()) {
            Long id = entry.getKey();
            FileMeta meta = entry.getValue();

            try {
                URL url = generatePreviewUrl(meta.key(), meta.contentType());
                result.put(id, url.toString());
            } catch (Exception e) {
                // presigned 생성 실패 시 null 대신 원래 key라도 리턴
                result.put(id, "home/" + meta.key());
                System.err.println("[S3Service] Presigned URL 생성 실패: " + meta.key() + " → " + e.getMessage());
            }
        }

        return result;
    }

    /* ===========================================================
        내부 헬퍼 메서드
       =========================================================== */

    /**
     * S3 Key 생성
     * 예: home/{UUID}.png
     */
    private String buildS3Key(String originalFileName) {
        try {
            String extension = "";

            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }

            String uuid = UUID.randomUUID().toString();
            return "home/" + uuid + extension;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "파일 이름이 유효하지 않습니다.");
        }
    }

    /**
     * MIME 타입별 Content-Type 반환
     */
    private String resolveContentType(String type) {
        if (type == null) return "application/octet-stream";
        if (type.startsWith("image/")) return type; // image/png, image/jpeg
        if (type.startsWith("text/")) return "text/plain; charset=utf-8";
        return type;
    }
    public String generatePreviewUrl(FileMeta meta) {
        // 단건 Map을 만들어서 기존 메서드 재사용
        return generatePreviewUrls(Map.of(1L, meta))   // Map<Long, FileMeta>
                .values()
                .stream()
                .findFirst()
                .orElse(null);
    }



    /**
     * 내부용 파일 메타데이터 구조체
     */
    public record FileMeta(String key, String contentType) {}
}

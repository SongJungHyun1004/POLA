package com.jinjinjara.pola.s3.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.s3.dto.response.S3PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URL;
import java.time.Duration;
import java.util.*;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final S3Client s3Client;
    private final FileRepository fileRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final Duration SIGN_DURATION = Duration.ofHours(1);

    /* 업로드용 presigned URL 생성 (home/original/) */
    public S3PresignedUrlResponse generateUploadUrl(String originalFileName) {
        try {
            String key = buildS3Key(originalFileName);

            PutObjectRequest objectRequest = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build();

            PresignedPutObjectRequest presignedRequest =
                    s3Presigner.presignPutObject(builder ->
                            builder.signatureDuration(Duration.ofMinutes(10))
                                    .putObjectRequest(objectRequest));

            return new S3PresignedUrlResponse(
                    presignedRequest.url(),
                    key
            );
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAIL, e.getMessage());
        }
    }

    /* 원본 다운로드 URL */
    public URL generateDownloadUrl(String key) {
        try {
            String fileName = extractFileName(key);

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .responseContentDisposition("attachment; filename=\"" + fileName + "\"")
                    .build();

            PresignedGetObjectRequest presignedRequest =
                    s3Presigner.presignGetObject(builder ->
                            builder.signatureDuration(Duration.ofMinutes(10))
                                    .getObjectRequest(getRequest));

            return presignedRequest.url();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    public URL generateDownloadUrlByFileId(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        return generateDownloadUrl(file.getSrc());
    }


    /* 최적화된 Preview URL 생성 */
    public URL generatePreviewUrl(String key, String contentType) {

        if (isTextType(contentType)) {
            return presignedInlineUrl(key, contentType);
        }

        String previewKey = key.replace("home/original/", "home/preview/");

        try {
            return presignedInlineUrl(previewKey, contentType);
        } catch (Exception e) {
            return presignedInlineUrl(key, contentType); // preview 미존재 → original fallback
        }
    }


    /* 실제 presigned URL 생성 (inline 보기) */
    private URL presignedInlineUrl(String key, String contentType) {

        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentType(resolveContentType(contentType))
                .responseContentDisposition("inline")
                .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(builder ->
                        builder.signatureDuration(SIGN_DURATION)
                                .getObjectRequest(getRequest));

        return presignedRequest.url();
    }
    /* 리마인드 전용: Preview URL 24시간 TTL */
    public Map<Long, String> generatePreviewUrlsLongTTL(Map<Long, FileMeta> fileMetaMap) {

        Map<Long, String> result = new HashMap<>(fileMetaMap.size());

        for (Map.Entry<Long, FileMeta> entry : fileMetaMap.entrySet()) {
            Long id = entry.getKey();
            FileMeta meta = entry.getValue();


            if (isTextType(meta.contentType())) {
                result.put(id, presignedInlineUrlLongTTL(meta.key(), meta.contentType()).toString());
                continue;
            }

            String previewKey = meta.key().replace("home/original/", "home/preview/");

            try {
                result.put(id, presignedInlineUrlLongTTL(previewKey, meta.contentType()).toString());
            } catch (Exception e) {
                // preview 없으면 original 키로 재시도
                result.put(id, presignedInlineUrlLongTTL(meta.key(), meta.contentType()).toString());
            }
        }

        return result;
    }



    /* 긴 TTL (24시간) presigned URL - 리마인드 전용 */
    private URL presignedInlineUrlLongTTL(String key, String contentType) {

        Duration longDuration = Duration.ofHours(25); // 24시간 고정

        GetObjectRequest getRequest = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .responseContentType(resolveContentType(contentType))
                .responseContentDisposition("inline")
                .build();

        PresignedGetObjectRequest presignedRequest =
                s3Presigner.presignGetObject(builder ->
                        builder.signatureDuration(longDuration)
                                .getObjectRequest(getRequest)
                );

        return presignedRequest.url();
    }


    /* 여러 파일의 Preview URL 일괄 생성 */
    public Map<Long, String> generatePreviewUrls(Map<Long, FileMeta> fileMetaMap) {

        Map<Long, String> result = new HashMap<>(fileMetaMap.size());

        for (Map.Entry<Long, FileMeta> entry : fileMetaMap.entrySet()) {
            Long id = entry.getKey();
            FileMeta meta = entry.getValue();

            // **텍스트 파일은 preview 사용 금지**
            if (isTextType(meta.contentType())) {
                result.put(id, presignedInlineUrl(meta.key(), meta.contentType()).toString());
                continue;
            }

            String previewKey = meta.key().replace("home/original/", "home/preview/");

            try {
                result.put(id, presignedInlineUrl(previewKey, meta.contentType()).toString());
            } catch (Exception e) {
                result.put(id, presignedInlineUrl(meta.key(), meta.contentType()).toString());
            }
        }
        return result;
    }


    /* 원본 미리보기 */
    public String generateOriginalPreviewUrl(String key, String contentType) {
        try {
            String originalKey = key.replace("home/preview/", "home/original/");

            return presignedInlineUrl(originalKey, contentType).toString();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    /* S3 파일 삭제 */
    public void deleteFileFromS3(String key) {
        try {

            String originalKey = key.replace("home/preview/", "home/original/");
            String previewKey = originalKey.replace("home/original/", "home/preview/");

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(originalKey)
                    .build());

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(previewKey)
                    .build());

        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_DELETE_FAIL, e.getMessage());
        }
    }

    /* 공유용 presigned URL */
    public String generateGetUrl(String key, boolean allowDownload) {
        try {
            String originalKey = key.replace("home/preview/", "home/original/");
            String fileName = extractFileName(originalKey);
            String disposition = allowDownload ? "attachment" : "inline";

            GetObjectRequest req = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(originalKey)
                    .responseContentDisposition(disposition + "; filename=\"" + fileName + "\"")
                    .build();

            return s3Presigner.presignGetObject(builder ->
                    builder.signatureDuration(Duration.ofHours(1))
                            .getObjectRequest(req)
            ).url().toString();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    /* ─────────────────────── Helper Methods ─────────────────────── */

    private String buildS3Key(String originalFileName) {
        try {
            String ext = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                ext = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            return "home/original/" + UUID.randomUUID() + ext;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "파일 이름이 유효하지 않습니다.");
        }
    }

    private String extractFileName(String key) {
        if (key == null || !key.contains("/")) return key != null ? key : "file";
        return key.substring(key.lastIndexOf("/") + 1);
    }

    private String resolveContentType(String type) {
        if (type == null) return "application/octet-stream";
        if (type.startsWith("image/")) return type;
        if (type.startsWith("text/")) return "text/plain; charset=utf-8";
        return type;
    }
    private boolean isTextType(String type) {
        if (type == null) return false;
        return type.startsWith("text/")
                || type.equals("application/json")
                || type.equals("application/xml")
                || type.equals("application/javascript")
                || type.equals("application/x-yaml");
    }

    public record FileMeta(String key, String contentType) {}
}

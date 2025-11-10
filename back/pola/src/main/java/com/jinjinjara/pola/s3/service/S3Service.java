package com.jinjinjara.pola.s3.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.FileRepository;
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

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Presigner s3Presigner;
    private final FileRepository fileRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

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

    public URL generateDownloadUrl(String key) {
        try {
            String fileName = extractFileName(key);

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .responseContentDisposition("attachment; filename=\"" + fileName + "\"")
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder ->
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

    public URL generatePreviewUrl(String key, String contentType) {
        try {
            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .responseContentType(resolveContentType(contentType))
                    .responseContentDisposition("inline")
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder ->
                    builder.signatureDuration(Duration.ofMinutes(10))
                            .getObjectRequest(getRequest));

            return presignedRequest.url();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    public Map<Long, String> generatePreviewUrls(Map<Long, FileMeta> fileMetaMap) {
        Map<Long, String> result = new HashMap<>();

        for (Map.Entry<Long, FileMeta> entry : fileMetaMap.entrySet()) {
            Long id = entry.getKey();
            FileMeta meta = entry.getValue();

            try {
                URL url = generatePreviewUrl(meta.key(), meta.contentType());
                result.put(id, url.toString());
            } catch (Exception e) {
                result.put(id, "home/" + meta.key());
                System.err.println("[S3Service] Presigned URL 생성 실패: " + meta.key() + " -> " + e.getMessage());
            }
        }

        return result;
    }

    public String generatePreviewUrl(FileMeta meta) {
        return generatePreviewUrls(Map.of(1L, meta))
                .values()
                .stream()
                .findFirst()
                .orElse(null);
    }

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

    private String extractFileName(String key) {
        if (key == null || !key.contains("/")) {
            return key != null ? key : "file";
        }
        return key.substring(key.lastIndexOf("/") + 1);
    }

    private String resolveContentType(String type) {
        if (type == null) return "application/octet-stream";
        if (type.startsWith("image/")) return type;
        if (type.startsWith("text/")) return "text/plain; charset=utf-8";
        return type;
    }

    public record FileMeta(String key, String contentType) {}
}

package com.jinjinjara.pola.s3.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.s3.dto.response.S3PresignedUrlResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
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

    // 업로드용 presigned URL 생성 (home/original/ 경로)
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

    // 다운로드 presigned URL (항상 original 기준)
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
        boolean isImage = contentType != null && (
                contentType.startsWith("image/") ||
                        key.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")
        );

        try {
            if (isImage) {
                String previewKey = key.replace("home/original/", "home/preview/");
                try {
                    S3Client s3Client = S3Client.builder()
                            .region(Region.AP_NORTHEAST_2)
                            .build();
                    s3Client.headObject(HeadObjectRequest.builder()
                            .bucket(bucket)
                            .key(previewKey)
                            .build());

                    // 존재하면 presigned URL 생성
                    GetObjectRequest previewRequest = GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(previewKey)
                            .responseContentType(resolveContentType(contentType))
                            .responseContentDisposition("inline")
                            .build();

                    PresignedGetObjectRequest presignedPreview = s3Presigner.presignGetObject(builder ->
                            builder.signatureDuration(Duration.ofMinutes(10))
                                    .getObjectRequest(previewRequest));

                    System.out.println("[S3Service] Using preview: " + previewKey);
                    return presignedPreview.url();

                } catch (S3Exception e) {
                    if (e.statusCode() == 404) {
                        System.out.println("[S3Service] Preview not found, fallback to original: " + key);
                    } else {
                        throw e;
                    }
                }
            }

            // fallback → 원본 URL
            GetObjectRequest originalRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .responseContentType(resolveContentType(contentType))
                    .responseContentDisposition("inline")
                    .build();

            PresignedGetObjectRequest presignedOriginal = s3Presigner.presignGetObject(builder ->
                    builder.signatureDuration(Duration.ofMinutes(10))
                            .getObjectRequest(originalRequest));

            return presignedOriginal.url();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    // 여러 파일에 대한 미리보기 URL 일괄 생성
    public Map<Long, String> generatePreviewUrls(Map<Long, FileMeta> fileMetaMap) {
        Map<Long, String> result = new HashMap<>();

        for (Map.Entry<Long, FileMeta> entry : fileMetaMap.entrySet()) {
            Long id = entry.getKey();
            FileMeta meta = entry.getValue();
            try {
                URL url = generatePreviewUrl(meta.key(), meta.contentType());
                result.put(id, url.toString());
            } catch (Exception e) {
                System.out.println("[S3Service] Preview URL 생성 실패, fallback: " + meta.key());
                result.put(id, "https://s3.ap-northeast-2.amazonaws.com/" + bucket + "/" + meta.key());
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

    // 원본 파일 미리보기 (inline)
    public String generateOriginalPreviewUrl(String key, String contentType) {
        try {
            String originalKey = key.contains("home/preview/")
                    ? key.replace("home/preview/", "home/original/")
                    : key;

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(originalKey)
                    .responseContentType(resolveContentType(contentType))
                    .responseContentDisposition("inline")
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder ->
                    builder.signatureDuration(Duration.ofMinutes(10))
                            .getObjectRequest(getRequest));

            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    // 파일 삭제 (original + preview)
    public void deleteFileFromS3(String key) {
        try {
            S3Client s3Client = S3Client.builder()
                    .region(Region.AP_NORTHEAST_2)
                    .build();

            String originalKey = key.contains("home/preview/")
                    ? key.replace("home/preview/", "home/original/")
                    : key;

            String previewKey = originalKey.replace("home/original/", "home/preview/");

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(originalKey)
                    .build());
            System.out.println("[S3Service] Deleted original: " + originalKey);

            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(previewKey)
                    .build());
            System.out.println("[S3Service] Deleted preview: " + previewKey);

        } catch (Exception e) {
            System.out.println("[S3Service] File delete failed: " + key + " -> " + e.getMessage());
            throw new CustomException(ErrorCode.FILE_DELETE_FAIL, e.getMessage());
        }
    }

    // 공유용 URL 생성
    public String generateGetUrl(String key, boolean allowDownload) {
        try {
            String originalKey = key.replace("home/preview/", "home/original/");
            String fileName = extractFileName(originalKey);
            String dispositionType = allowDownload ? "attachment" : "inline";

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(originalKey)
                    .responseContentDisposition(dispositionType + "; filename=\"" + fileName + "\"")
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder ->
                    builder.signatureDuration(Duration.ofMinutes(10))
                            .getObjectRequest(getRequest));

            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    private String buildS3Key(String originalFileName) {
        try {
            String extension = "";
            if (originalFileName != null && originalFileName.contains(".")) {
                extension = originalFileName.substring(originalFileName.lastIndexOf("."));
            }
            String uuid = UUID.randomUUID().toString();
            return "home/original/" + uuid + extension;
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

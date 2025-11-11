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
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
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

    /**
     *  Presigned URL 생성 (업로드용)
     * - 파일은 항상 home/original/ 경로로 저장
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

    /**
     * 다운로드 Presigned URL 생성 (원본 기준)
     */
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

    /**
     *  미리보기 URL 생성 (이미지는 preview, 그 외는 original)
     */
    public URL generatePreviewUrl(String key, String contentType) {
        try {
            boolean isImage = contentType != null && (
                    contentType.startsWith("image/") ||
                            key.toLowerCase().matches(".*\\.(jpg|jpeg|png)$")
            );

            // 이미지인 경우 preview 경로 우선 시도
            if (isImage) {
                String previewKey = key.replace("home/original/", "home/preview/");
                try {
                    GetObjectRequest previewRequest = GetObjectRequest.builder()
                            .bucket(bucket)
                            .key(previewKey)
                            .responseContentType(resolveContentType(contentType))
                            .responseContentDisposition("inline")
                            .build();

                    PresignedGetObjectRequest presignedPreview = s3Presigner.presignGetObject(builder ->
                            builder.signatureDuration(Duration.ofMinutes(10))
                                    .getObjectRequest(previewRequest));

                    return presignedPreview.url(); // preview 성공 시 바로 리턴
                } catch (Exception e) {
                    //  preview 파일이 없는 경우 original로 fallback
                    System.err.println("[S3Service] Preview not found, falling back to original: " + key);
                }
            }

            //  fallback: preview가 없거나 이미지가 아닌 경우 → original 반환
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
            //  최종 실패 시 예외 처리
            System.err.println("[S3Service] Preview & original URL generation failed: " + key);
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }

    /**
     *  여러 파일에 대한 미리보기 URL 생성 (안전 fallback 포함)
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
                System.err.println("[S3Service] Preview URL 생성 실패, fallback: " + meta.key());
                result.put(id, "https://s3." + "ap-northeast-2" + ".amazonaws.com/" + bucket + "/" + meta.key());
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

    /**
     * 원본 파일 미리보기용 Presigned URL 생성
     * - 이미지, 텍스트, PDF 등 브라우저에서 바로 볼 수 있게 함
     * - 다운로드가 아닌 inline 모드
     */
    public String generateOriginalPreviewUrl(String key, String contentType) {
        try {
            // 항상 원본 경로 기준 (혹시 preview 경로 들어와도 자동 변환)
            String originalKey = key.contains("home/preview/")
                    ? key.replace("home/preview/", "home/original/")
                    : key;

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(originalKey)
                    .responseContentType(resolveContentType(contentType))
                    .responseContentDisposition("inline") // 브라우저 미리보기용
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder ->
                    builder.signatureDuration(Duration.ofMinutes(10))  // 10분 유효
                            .getObjectRequest(getRequest));

            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }



    /**
     *  S3 저장 경로: home/original/{UUID}.ext
     */
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

    /**
     *  공유용 URL 생성 (다운로드 / 미리보기 구분)
     */
    public String generateGetUrl(String key, boolean allowDownload) {
        try {
            // 항상 원본 경로로 통일
            String originalKey = key.replace("home/preview/", "home/original/");

            String fileName = extractFileName(originalKey);
            String dispositionType = allowDownload ? "attachment" : "inline";

            GetObjectRequest getRequest = GetObjectRequest.builder()
                    .bucket(bucket)
                    .key(originalKey)
                    .responseContentDisposition(dispositionType + "; filename=\"" + fileName + "\"")
                    .build();

            PresignedGetObjectRequest presignedRequest = s3Presigner.presignGetObject(builder ->
                    builder.signatureDuration(Duration.ofMinutes(10))  // URL 유효기간 10분
                            .getObjectRequest(getRequest));

            return presignedRequest.url().toString();

        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_NOT_FOUND, e.getMessage());
        }
    }


    public void deleteFileFromS3(String key) {
        try {
            //  리전 명시
            S3Client s3Client = S3Client.builder()
                    .region(Region.AP_NORTHEAST_2) // 서울 리전
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
            System.err.println("[S3Service] File delete failed: " + key + " -> " + e.getMessage());
            throw new CustomException(ErrorCode.FILE_DELETE_FAIL, e.getMessage());
        }
    }

    public record FileMeta(String key, String contentType) {}
}

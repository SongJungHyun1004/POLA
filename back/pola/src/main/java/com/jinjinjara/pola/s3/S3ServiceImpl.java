package com.jinjinjara.pola.s3;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import lombok.RequiredArgsConstructor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@RequiredArgsConstructor
@Service
public class S3ServiceImpl implements S3Service {
    private final AmazonS3 amazonS3;

    @Value("${aws.s3.bucket}")
    private String bucket;

    // 파일 업로드
    @Override
    public String uploadFile(MultipartFile multipartFile, String url) {
        String fileName = url+"/"+ UUID.randomUUID().toString() + "_" + multipartFile.getOriginalFilename();
        ObjectMetadata objMeta = new ObjectMetadata();
        objMeta.setContentLength(multipartFile.getSize());
        objMeta.setContentType(multipartFile.getContentType());
        try {
            amazonS3.putObject(bucket, fileName, multipartFile.getInputStream(), objMeta);
        } catch (IOException e) {
            throw new RuntimeException("S3 업로드 중 에러가 발생했습니다", e);
        }
        return amazonS3.getUrl(bucket, fileName).toString();
    }
    // 파일 삭제
    @Override
    public boolean delete(String fileUrl) {
        try {
            String[] temp = fileUrl.split(".com/");
            String fileKey = temp[1];
            amazonS3.deleteObject(bucket, fileKey);
            return true;
        } catch (Exception e) {
            log.warn("S3 파일 삭제 중 에러가 발생했습니다. fileUrl: {}, error: {}", fileUrl, e.getMessage());
            return false;
        }
    }}
package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.data.dto.request.FileUploadCompleteRequest;
import com.jinjinjara.pola.data.dto.response.InsertDataResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class DataService {

    private final FileRepository fileRepository;
    private final CategoryRepository categoryRepository;

    /**
     * Presigned URL 업로드 완료 후 DB 메타데이터 저장
     */
    @Transactional
    public File saveUploadedFile(Users user, FileUploadCompleteRequest request) {

        // 사용자별 "미분류" 카테고리 확인 또는 생성
        Category uncategorized = categoryRepository
                .findByUserAndCategoryName(user, "미분류")
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .user(user)
                            .categoryName("미분류")
                            .build();
                    return categoryRepository.save(newCategory);
                });

        //  File 엔티티 생성 (DB 스키마 기준)
        File file = File.builder()
                .userId(user.getId())
                .categoryId(uncategorized.getId())
                .src(request.getKey())                     // S3 key (e.g. home/uuid.jpg)
                .type(request.getType())                   // MIME type (e.g. image/jpeg)
                .context("Llava")                          // 기본값 (NOT NULL)
                .fileSize((long) request.getFileSize())
                .originUrl(request.getOriginUrl())
                .platform("S3")                            // 업로드 플랫폼 (기본값)
                .shareStatus(false)
                .favorite(false)
                .favoriteSort(0)
                .favoritedAt(LocalDateTime.now())
                .views(0)
                .build();

        return fileRepository.save(file);
    }

    @Transactional
    public File updateFileCategory(Long fileId, String newCategoryName, Users user) {
        //  파일 조회
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다. ID = " + fileId));

        //  사용자 권한 확인 (본인 파일만 변경 가능)
        if (!file.getUserId().equals(user.getId())) {
            throw new SecurityException("본인의 파일만 카테고리를 변경할 수 있습니다.");
        }

        //  변경할 카테고리 조회 또는 생성
        Category targetCategory = categoryRepository
                .findByUserAndCategoryName(user, newCategoryName)
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .user(user)
                            .categoryName(newCategoryName)
                            .build();
                    return categoryRepository.save(newCategory);
                });

        //  파일의 categoryId 업데이트
        file.setCategoryId(targetCategory.getId());

        //  DB에 반영 (JPA 변경감지로 UPDATE 실행)
        return fileRepository.save(file);
    }

    /**
     * 테스트용 목업 데이터 삽입
     */
    public InsertDataResponse insertData(MultipartFile file, String originUrl, com.jinjinjara.pola.data.dto.common.Platform platform) {
        if (file == null || file.isEmpty()) {
            // throw new FileProcessException("파일이 비어 있습니다.");
        }

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String s3Url = "https://s3-bucket/path/to/" + file.getOriginalFilename();

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

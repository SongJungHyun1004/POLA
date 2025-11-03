package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.data.dto.common.Platform;
import com.jinjinjara.pola.data.dto.request.FileUploadCompleteRequest;
import com.jinjinjara.pola.data.dto.response.InsertDataResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.FileEntity;
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


    @Transactional
    public FileEntity saveUploadedFile(Users user, FileUploadCompleteRequest request) {

        Category uncategorized = categoryRepository
                .findByUserIdAndCategoryName(user, "미분류")
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .user(user)
                            .categoryName("미분류")
                            .build();
                    return categoryRepository.save(newCategory);
                });

        FileEntity file = FileEntity.builder()
                .userId(user.getId())
                .categoryId(uncategorized.getId().intValue()) // 🔹 category_id FK 저장
                .src(request.getKey())
                .type(request.getType())
                .fileSize(request.getFileSize())
                .originUrl(request.getOriginUrl())
                .favorite(false)
                .shareStatus(false)
                .build();

        return fileRepository.save(file);
    }

    /**
     * ✅ 테스트용 데이터 삽입 (목업)
     */
    public InsertDataResponse insertData(MultipartFile file, String originUrl, Platform platform) {
        if (file == null || file.isEmpty()) {
            // throw new FileProcessException("파일이 비어 있습니다.");
        }

        // MIME 타입 확인
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        String s3Url = "https://s3-bucket/path/to/" + file.getOriginalFilename();

        // AI 처리 결과 (목업)
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

package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
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
import java.util.List;
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

        File file = File.builder()
                .userId(user.getId())
                .categoryId(uncategorized.getId())
                .src(request.getKey())                     // S3 key
                .type(request.getType())                   // MIME type
                .context("Llava")
                .fileSize((long) request.getFileSize())
                .originUrl(request.getOriginUrl())
                .platform("S3")
                .shareStatus(false)
                .favorite(false)
                .favoriteSort(0)
                .favoritedAt(LocalDateTime.now())
                .views(0)
                .build();

        return fileRepository.save(file);
    }

    /**
     * 파일 카테고리 변경
     */
    @Transactional
    public File updateFileCategory(Long fileId, String newCategoryName, Users user) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!file.getUserId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FILE_ACCESS_DENIED);
        }

        Category targetCategory = categoryRepository
                .findByUserAndCategoryName(user, newCategoryName)
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .user(user)
                            .categoryName(newCategoryName)
                            .build();
                    return categoryRepository.save(newCategory);
                });

        file.setCategoryId(targetCategory.getId());
        return fileRepository.save(file);
    }

    /* =======================================================
        즐겨찾기 관련 기능
       ======================================================= */

    /**
     * 즐겨찾기 추가
     */
    @Transactional
    public File addFavorite(Long fileId, Integer sortValue, Users user) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!file.getUserId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FILE_ACCESS_DENIED);
        }

        file.setFavorite(true);
        file.setFavoriteSort(sortValue != null ? sortValue : 0);
        file.setFavoritedAt(LocalDateTime.now());

        return fileRepository.save(file);
    }

    /**
     * 즐겨찾기 제거
     */
    @Transactional
    public File removeFavorite(Long fileId, Users user) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!file.getUserId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FILE_ACCESS_DENIED);
        }

        file.setFavorite(false);
        file.setFavoriteSort(0);
        file.setFavoritedAt(null);

        return fileRepository.save(file);
    }
    //즐겨찾기 파일 조회
    @Transactional(readOnly = true)
    public List<File> getFavoriteFiles(Users user) {
        if (user == null) {
            throw new CustomException(ErrorCode.USER_UNAUTHORIZED);
        }

        List<File> favorites = fileRepository
                .findAllByUserIdAndFavoriteTrueOrderByFavoriteSortAscFavoritedAtDesc(user.getId());

        if (favorites.isEmpty()) {
            throw new CustomException(ErrorCode.DATA_NOT_FOUND, "즐겨찾기된 파일이 없습니다.");
        }

        return favorites;
    }

    /**
     * 즐겨찾기 순서(favoriteSort) 변경
     */
    @Transactional
    public File updateFavoriteSort(Long fileId, Integer newSort, Users user) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!file.getUserId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FILE_ACCESS_DENIED);
        }

        if (!file.getFavorite()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "즐겨찾기 상태가 아닙니다. 순서를 변경할 수 없습니다.");
        }

        file.setFavoriteSort(newSort);
        return fileRepository.save(file);
    }

    /**
     * 테스트용 목업 데이터 삽입
     */
    public InsertDataResponse insertData(MultipartFile file, String originUrl, com.jinjinjara.pola.data.dto.common.Platform platform) {
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

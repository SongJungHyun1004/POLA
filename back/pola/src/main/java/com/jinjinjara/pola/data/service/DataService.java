package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.common.dto.PageRequestDto;
import com.jinjinjara.pola.data.dto.request.FileUploadCompleteRequest;
import com.jinjinjara.pola.data.dto.response.DataResponse;
import com.jinjinjara.pola.data.dto.response.FileDetailResponse;
import com.jinjinjara.pola.data.dto.response.InsertDataResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DataService {

    private final FileRepository fileRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;

    public List<DataResponse> getRemindFiles(Long userId) {
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);

        List<File> files = fileRepository.findRemindFiles(userId, sevenDaysAgo, PageRequest.of(0, 30));

        return files.stream()
                .map(file -> DataResponse.builder()
                        .id(file.getId())
                        .src(file.getSrc())
                        .type(file.getType())
                        .context(file.getContext())
                        .favorite(file.getFavorite())
                        .build())
                .toList();
    }

    @Transactional
    public FileDetailResponse getFileDetail(Long userId, Long fileId) {
        File file = fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        file.setViews(file.getViews() + 1);
        file.setLastViewedAt(LocalDateTime.now());
        fileRepository.save(file);

        return FileDetailResponse.builder()
                .id(file.getId())
                .userId(file.getUserId())
                .categoryId(file.getCategoryId())
                .src(file.getSrc())
                .type(file.getType())
                .context(file.getContext())
                .ocrText(file.getOcrText())
                .vectorId(file.getVectorId())
                .fileSize(file.getFileSize())
                .shareStatus(file.getShareStatus())
                .favorite(file.getFavorite())
                .favoriteSort(file.getFavoriteSort())
                .favoritedAt(file.getFavoritedAt())
                .views(file.getViews())
                .platform(file.getPlatform())
                .originUrl(file.getOriginUrl())
                .createdAt(file.getCreatedAt())
                .lastViewedAt(file.getLastViewedAt())
                .build();
    }



    /**
     * 파일 목록 조회 (페이징 + 정렬 + 필터 + Presigned URL)
     */
    public Page<DataResponse> getFiles(Users user, PageRequestDto request) {
        if (user == null) {
            throw new CustomException(ErrorCode.USER_UNAUTHORIZED);
        }

        Pageable pageable = request.toPageable();

        // 필터 타입 분기
        Page<File> files = switch (request.getFilterType() == null ? "" : request.getFilterType()) {
            case "category" -> {
                if (request.getFilterId() == null)
                    throw new CustomException(ErrorCode.INVALID_REQUEST, "카테고리 ID가 필요합니다.");
                yield fileRepository.findAllByUserIdAndCategoryId(user.getId(), request.getFilterId(), pageable);
            }
            case "favorite" -> fileRepository.findAllByUserIdAndFavoriteTrue(user.getId(), pageable);
            default -> fileRepository.findAllByUserId(user.getId(), pageable);
        };

        // presigned URL 매핑 (id → key, type)
        Map<Long, S3Service.FileMeta> metaMap = files.stream()
                .collect(Collectors.toMap(
                        File::getId,
                        f -> new S3Service.FileMeta(f.getOriginUrl(), f.getType())
                ));


        Map<Long, String> previewUrls = s3Service.generatePreviewUrls(metaMap);

        // 변환: File → DataResponse
        return files.map(file -> DataResponse.builder()
                .id(file.getId())
                .src(previewUrls.get(file.getId()))  // ✅ 미리보기용 presigned URL
                .type(file.getType())
                .context(file.getContext())
                .favorite(file.getFavorite())
                .build());
    }

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
//    //즐겨찾기 파일 조회
//    @Transactional(readOnly = true)
//    public List<File> getFavoriteFiles(Users user) {
//        if (user == null) {
//            throw new CustomException(ErrorCode.USER_UNAUTHORIZED);
//        }
//
//        List<File> favorites = fileRepository
//                .findAllByUserIdAndFavoriteTrueOrderByFavoriteSortAscFavoritedAtDesc(user.getId());
//
//        if (favorites.isEmpty()) {
//            throw new CustomException(ErrorCode.DATA_NOT_FOUND, "즐겨찾기된 파일이 없습니다.");
//        }
//
//        return favorites;
//    }

    /**
     * 즐겨찾기 순서 변경 (정렬 구간 밀기 방식)
     */
    public File updateFavoriteSort(Long fileId, int newSort, Users user) {
        if (user == null) {
            throw new CustomException(ErrorCode.USER_UNAUTHORIZED);
        }

        File target = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        if (!target.getUserId().equals(user.getId())) {
            throw new CustomException(ErrorCode.FILE_ACCESS_DENIED);
        }

        if (!target.getFavorite()) {
            throw new CustomException(ErrorCode.INVALID_REQUEST, "즐겨찾기 상태가 아닌 파일은 순서를 변경할 수 없습니다.");
        }

        int oldSort = target.getFavoriteSort();

        if (newSort == oldSort) return target; // 순서 동일 → 변경 없음

        // 순서 이동
        if (newSort < oldSort) {
            // 위로 이동 → 해당 구간 sort + 1
            fileRepository.incrementSortRange(user.getId(), newSort, oldSort);
        } else {
            // 아래로 이동 → 해당 구간 sort - 1
            fileRepository.decrementSortRange(user.getId(), oldSort, newSort);
        }

        // 대상 파일 sort 갱신
        target.setFavoriteSort(newSort);
        return fileRepository.save(target);
    }

}

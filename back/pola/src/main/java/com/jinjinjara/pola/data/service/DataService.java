package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.common.dto.PageRequestDto;
import com.jinjinjara.pola.data.dto.request.FileShareRequest;
import com.jinjinjara.pola.data.dto.request.FileUpdateRequest;
import com.jinjinjara.pola.data.dto.request.FileUploadCompleteRequest;
import com.jinjinjara.pola.data.dto.response.DataResponse;
import com.jinjinjara.pola.data.dto.response.FileDetailResponse;
import com.jinjinjara.pola.data.dto.response.FileShareResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.entity.FileTag;
import com.jinjinjara.pola.data.entity.Tag;
import com.jinjinjara.pola.data.repository.*;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.data.repository.TagRepository;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.search.model.FileSearch;
import com.jinjinjara.pola.search.service.FileSearchService;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.vision.dto.response.AnalyzeResponse;
import com.jinjinjara.pola.vision.entity.FileEmbeddings;
import com.jinjinjara.pola.vision.repository.FileEmbeddingsRepository;
import com.jinjinjara.pola.vision.service.AnalyzeFacadeService;
import com.jinjinjara.pola.vision.service.EmbeddingService;
import com.jinjinjara.pola.vision.service.VisionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StopWatch;

import java.net.URL;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DataService {

    private final FileRepository fileRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final TagRepository tagRepository;
    private final AnalyzeFacadeService analyzeFacadeService;
    private final FileTagService fileTagService;
    private final VisionService visionService;
    private final EmbeddingService embeddingService;
    private final FileEmbeddingsRepository fileEmbeddingsRepository;
    private final CategoryTagRepository categoryTagRepository;
    private final FileTagRepository fileTagRepository;
    private final FileSearchService fileSearchService;
    private final RemindCacheRepository remindCacheRepository;

    @Transactional(readOnly = true)
    public List<DataResponse> getRemindFiles(Long userId) {

        List<DataResponse> cached = remindCacheRepository.getRemindFiles(userId);

        if (cached == null) {
            log.debug("[Remind] Redis miss for userId={}, return null", userId);
            return null;
        }

        log.debug("[Remind] Redis hit for userId={}", userId);
        return cached;
    }



    @Transactional(readOnly = true)
    public List<DataResponse> buildRemindFiles(Long userId) {

        List<File> files = fileRepository.findLeastViewedFiles(
                userId,
                PageRequest.of(0, 30)
        );

        if (files.isEmpty()) return List.of();

        Map<Long, S3Service.FileMeta> metaMap = files.stream()
                .collect(Collectors.toMap(
                        File::getId,
                        f -> new S3Service.FileMeta(f.getSrc(), f.getType())
                ));

        Map<Long, String> previewUrls = s3Service.generatePreviewUrls(metaMap);

        List<Long> fileIds = files.stream().map(File::getId).toList();

        List<FileTag> fileTags = fileTagRepository.findAllByFileIds(fileIds);

        Map<Long, List<String>> tagMap = fileTags.stream()
                .collect(Collectors.groupingBy(
                        ft -> ft.getFile().getId(),
                        Collectors.mapping(ft -> ft.getTag().getTagName(), Collectors.toList())
                ));

        return files.stream()
                .map(file -> DataResponse.builder()
                        .id(file.getId())
                        .src(previewUrls.get(file.getId()))
                        .type(file.getType())
                        .context(file.getContext())
                        .ocrText(file.getOcrText())
                        .createdAt(file.getCreatedAt())
                        .favorite(file.getFavorite())
                        .tags(tagMap.getOrDefault(file.getId(), List.of()))
                        .build())
                .toList();
    }

    @Transactional
    public void deleteFile(Long fileId,Users user) {
        File file = fileRepository.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        try {
            Long categoryId = file.getCategoryId();
            Category category = categoryRepository.findById(categoryId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

            fileTagRepository.deleteByFile(file);
            s3Service.deleteFileFromS3(file.getSrc());
            // 3. OpenSearch에서 인덱스 삭제
            deleteFromOpenSearchAsync(fileId);


            fileRepository.delete(file);
            remindCacheRepository.removeItem(user.getId(), fileId);

            category.decreaseCount(1);
            categoryRepository.save(category);

        } catch (Exception e) {
            throw new CustomException(ErrorCode.FILE_DELETE_FAIL, e.getMessage());
        }
    }


    @Transactional
    public FileDetailResponse getFileDetail(Long userId, Long fileId) {
        File file = fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        // 조회수 및 마지막 열람 시각 갱신
        file.setViews(file.getViews() + 1);
        file.setLastViewedAt(LocalDateTime.now());
        fileRepository.save(file);

        // 🏷 파일에 연결된 태그 조회
        List<TagResponse> tags = tagRepository.findAllByFileId(fileId).stream()
                .map(tag -> TagResponse.builder()
                        .id(tag.getId())
                        .tagName(tag.getTagName())
                        .build())
                .toList();

        //원본 미리보기 URL 생성
        String presignedUrl = s3Service.generateOriginalPreviewUrl(
                file.getSrc(),
                file.getType()
        );


        // 응답 DTO 구성
        return FileDetailResponse.builder()
                .id(file.getId())
                .userId(file.getUserId())
                .categoryId(file.getCategoryId())
                .src(presignedUrl) // presigned URL 반환
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
                .tags(tags)
                .build();
    }


    @Transactional(readOnly = true)
    public Page<DataResponse> getFiles(Users user, PageRequestDto request) {
        if (user == null) {
            throw new CustomException(ErrorCode.USER_UNAUTHORIZED);
        }

        Pageable pageable = request.toPageable();
        String filterType = request.getFilterType() == null ? "" : request.getFilterType();
        Long filterId = request.getFilterId();

        Page<File> files = switch (filterType) {
            case "category" -> {
                if (filterId == null)
                    throw new CustomException(ErrorCode.INVALID_REQUEST, "카테고리 ID가 필요합니다.");
                yield fileRepository.findAllByUserIdAndCategoryId(user.getId(), filterId, pageable);
            }
            case "favorite" -> fileRepository.findAllByUserIdAndFavoriteTrue(user.getId(), pageable);
            case "tag" -> {
                if (filterId == null)
                    throw new CustomException(ErrorCode.INVALID_REQUEST, "태그 ID가 필요합니다.");
                yield fileRepository.findAllByUserIdAndTagId(user.getId(), filterId, pageable);
            }
            default -> fileRepository.findAllByUserId(user.getId(), pageable);
        };

        Map<Long, S3Service.FileMeta> metaMap = files.stream()
                .collect(Collectors.toMap(
                        File::getId,
                        f -> new S3Service.FileMeta(f.getSrc(), f.getType())
                ));

        Map<Long, String> previewUrls = s3Service.generatePreviewUrls(metaMap);

        List<Long> fileIds = files.stream().map(File::getId).toList();
        List<FileTag> fileTags = fileTagRepository.findAllByFileIds(fileIds);
        Map<Long, List<String>> tagMap = fileTags.stream()
                .collect(Collectors.groupingBy(
                        ft -> ft.getFile().getId(),
                        Collectors.mapping(ft -> ft.getTag().getTagName(), Collectors.toList())
                ));

        return files.map(file -> DataResponse.builder()
                .id(file.getId())
                .src(previewUrls.get(file.getId()))
                .type(file.getType())
                .context(file.getContext())
                .ocrText(file.getOcrText())
                .favorite(file.getFavorite())
                .tags(tagMap.getOrDefault(file.getId(), List.of()))
                .createdAt(file.getCreatedAt())
                .build());
    }


    @Transactional(readOnly = true)
    public String getFilterName(String filterType, Long filterId) {
        if (filterType == null || filterType.isEmpty()) {
            return "all";
        }

        return switch (filterType) {
            case "category" -> {
                if (filterId == null) yield "category";
                yield categoryRepository.findById(filterId)
                        .map(Category::getCategoryName)
                        .orElse("category");
            }
            case "tag" -> {
                if (filterId == null) yield "tag";
                yield tagRepository.findById(filterId)
                        .map(Tag::getTagName)
                        .orElse("tag");
            }
            case "favorite" -> "favorite";
            default -> filterType;
        };
    }


    /**
     * Presigned URL 업로드 완료 후 DB 메타데이터 저장
     */
    @Transactional
    public File saveUploadedFile(Users user, FileUploadCompleteRequest request) {

        Category uncategorized = categoryRepository
                .findByUserAndCategoryName(user, "미분류")
                .orElseGet(() -> {
                    Category newCategory = Category.builder()
                            .user(user)
                            .categoryName("미분류")
                            .fileCount(0)
                            .build();
                    return categoryRepository.save(newCategory);
                });

        uncategorized.increaseCount(1);
        categoryRepository.save(uncategorized);

        File file = File.builder()
                .userId(user.getId())
                .categoryId(uncategorized.getId())
                .src(request.getKey())
                .type(request.getType())
                .context("AI가 파일을 해석 중입니다.")
                .fileSize((long) request.getFileSize())
                .originUrl(request.getOriginUrl())
                .platform(request.getPlatform())
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
    public File updateFileCategory(Long fileId, Long categoryId, Users user) {
        File file = fileRepository.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        Long oldCategoryId = file.getCategoryId();
        if (Objects.equals(oldCategoryId, categoryId)) {
            return file;
        }

        Category oldCategory = categoryRepository.findById(oldCategoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        Category newCategory = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        file.setCategoryId(categoryId);
        File savedFile = fileRepository.save(file);

        // OpenSearch 업데이트
        String categoryName = categoryRepository.findById(categoryId)
                .map(Category::getCategoryName)
                .orElse("미분류");
        oldCategory.decreaseCount(1);
        newCategory.increaseCount(1);

        categoryRepository.save(oldCategory);
        categoryRepository.save(newCategory);

        indexToOpenSearchAsync(savedFile, categoryName);

        return savedFile;
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

        File saved = fileRepository.save(file);

        // OpenSearch 업데이트
        String categoryName = categoryRepository.findById(file.getCategoryId())
                .map(Category::getCategoryName)
                .orElse("미분류");
        indexToOpenSearchAsync(saved, categoryName);

        return saved;
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
//        file.setFavoritedAt(null);

        File saved = fileRepository.save(file);

        // OpenSearch 업데이트
        String categoryName = categoryRepository.findById(file.getCategoryId())
                .map(Category::getCategoryName)
                .orElse("미분류");
        indexToOpenSearchAsync(saved, categoryName);

        return saved;
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
    @Transactional
    public FileShareResponse createShareLink(Long userId, Long fileId, FileShareRequest request) {

        File file = fileRepository.findByIdAndUserId(fileId, userId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        log.info("[DEBUG] shareStatus={}, shareToken={}, expiredAt={}",
                file.getShareStatus(),
                file.getShareToken(),
                file.getShareExpiredAt()
        );

        LocalDateTime now = LocalDateTime.now();
        int expireHours = Optional.ofNullable(request.getExpireHours()).orElse(24);

        // 최초 공유
        if (!Boolean.TRUE.equals(file.getShareStatus()) || file.getShareToken() == null) {
            String token = UUID.randomUUID().toString();
            LocalDateTime expiredAt = now.plusHours(expireHours);

            file.setShareStatus(true);
            file.setShareToken(token);
            file.setShareExpiredAt(expiredAt);

            return FileShareResponse.builder()
                    .shareUrl(buildShareUrl(token))
                    .expiredAt(expiredAt.toString())
                    .build();
        }

        // 이미 공유됨
        LocalDateTime expiredAt = file.getShareExpiredAt();

        // 연장
        if (expiredAt != null && expiredAt.isAfter(now)) {
            LocalDateTime newExpiredAt = now.plusHours(expireHours);
            file.setShareExpiredAt(newExpiredAt);

            return FileShareResponse.builder()
                    .shareUrl(buildShareUrl(file.getShareToken()))
                    .expiredAt(newExpiredAt.toString())
                    .build();
        }

        // 만료됨 → 새 토큰 발급
        String newToken = UUID.randomUUID().toString();
        LocalDateTime newExpiredAt = now.plusHours(expireHours);

        file.setShareStatus(true);
        file.setShareToken(newToken);
        file.setShareExpiredAt(newExpiredAt);

        return FileShareResponse.builder()
                .shareUrl(buildShareUrl(newToken))
                .expiredAt(newExpiredAt.toString())
                .build();
    }



    //링크수정
    private String buildShareUrl(String token) {
        return String.format("%s", token);
    }

    public File findByShareToken(String token) {
        return fileRepository.findByShareToken(token)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
    }

    @Transactional
    public FileDetailResponse updateFileContext(Users user, Long fileId, FileUpdateRequest request) {
        File file = fileRepository.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        // context가 비어있지 않을 때만 수정
        if (request.getContext() != null && !request.getContext().isBlank()) {
            file.setContext(request.getContext());
        }

        File saved = fileRepository.save(file);

        // OpenSearch 업데이트
        String categoryName = categoryRepository.findById(saved.getCategoryId())
                .map(Category::getCategoryName)
                .orElse("미분류");
        indexToOpenSearchAsync(saved, categoryName);

        return FileDetailResponse.builder()
                .id(saved.getId())
                .userId(saved.getUserId())
                .categoryId(saved.getCategoryId())
                .src(saved.getSrc())
                .type(saved.getType())
                .context(saved.getContext())
                .ocrText(saved.getOcrText())
                .vectorId(saved.getVectorId())
                .fileSize(saved.getFileSize())
                .shareStatus(saved.getShareStatus())
                .favorite(saved.getFavorite())
                .favoriteSort(saved.getFavoriteSort())
                .favoritedAt(saved.getFavoritedAt())
                .views(saved.getViews())
                .platform(saved.getPlatform())
                .originUrl(saved.getOriginUrl())
                .createdAt(saved.getCreatedAt())
                .lastViewedAt(saved.getLastViewedAt())
                .build();
    }

    @Transactional
    public File postProcessingFile(Users user, Long fileId) throws Exception {

        StopWatch sw = new StopWatch("postProcess");
        log.info("[PostProcess] Start post-processing fileId={}, userId={}", fileId, user.getId());

        sw.start("Load File");
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        Long oldCategoryId = file.getCategoryId(); // 기존 카테고리 ID 저장
        log.info("[PostProcess] File entity loaded: src={}, oldCategoryId={}", file.getSrc(), oldCategoryId);
        sw.stop();

        sw.start("Load Url");
        URL downUrl = s3Service.generateDownloadUrl(file.getSrc());
        log.info("[PostProcess] S3 download URL generated: {}", downUrl);
        sw.stop();

        sw.start("OCR+Analyze");

        // OCR 비동기 실행
        CompletableFuture<String> ocrFuture = CompletableFuture.supplyAsync(() -> {
            long t0 = System.currentTimeMillis();
            try {
                String text = visionService.extractTextFromS3Url(downUrl.toString());
                long elapsed = System.currentTimeMillis() - t0;
                log.info("[PostProcess] OCR extraction completed: textLength={}, elapsed={} ms",
                        text != null ? text.length() : 0,
                        elapsed);
                return text;
            } catch (Exception e) {
                log.error("[PostProcess] OCR failed", e);
                throw new CompletionException(e);
            }
        });

        // Analyze 비동기 실행
        CompletableFuture<AnalyzeResponse> analyzeFuture = CompletableFuture.supplyAsync(() -> {
            long t0 = System.currentTimeMillis();
            try {
                AnalyzeResponse res = analyzeFacadeService.analyze(user.getId(), downUrl.toString());
                long elapsed = System.currentTimeMillis() - t0;
                log.info("[PostProcess] Analyze completed: categoryId={}, tagsCount={}, elapsed={} ms",
                        res.getCategoryId(),
                        res.getTags() != null ? res.getTags().size() : 0,
                        elapsed);
                return res;
            } catch (Exception e) {
                log.error("[PostProcess] Analyze failed", e);
                throw new CompletionException(e);
            }
        });

        String ocrText;
        AnalyzeResponse analyzeResponse;
        try {
            // 병렬 실행
            ocrText = ocrFuture.join();
            analyzeResponse = analyzeFuture.join();
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof Exception ex) {
                throw ex;
            }
            throw e;
        }

        Long newCategoryId = analyzeResponse.getCategoryId();
        log.info("[PostProcess] Analyze completed: newCategoryId={}", newCategoryId);

        sw.stop();

        sw.start("TagSave");
        fileTagService.addTagsToFile(fileId, analyzeResponse.getTags(),user);
        sw.stop();

        sw.start("Embedding");
        float[] embedding = embeddingService.embedOcrAndContext(ocrText, analyzeResponse.getDescription());
        sw.stop();

        sw.start("EmbeddingDBSave");
        FileEmbeddings fileEmbeddings = fileEmbeddingsRepository.save(
                FileEmbeddings.builder()
                        .userId(user.getId())
                        .file(file)
                        .ocrText(ocrText)
                        .context(analyzeResponse.getDescription())
                        .embedding(embedding)
                        .build()
        );
        sw.stop();

        sw.start("FileUpdate");
        fileRepository.updatePostProcessing(
                file.getId(),
                user.getId(),
                newCategoryId,
                analyzeResponse.getDescription(),
                ocrText,
                fileEmbeddings.getId()
        );
        sw.stop();

        /*  여기서 category 파일 개수 업데이트  */
        if (!Objects.equals(oldCategoryId, newCategoryId)) {
            Category oldCategory = categoryRepository.findById(oldCategoryId)
                    .orElse(null); // 혹시 삭제된 카테고리 예외 처리

            Category newCategory = categoryRepository.findById(newCategoryId)
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

            if (oldCategory != null) {
                oldCategory.decreaseCount(1);
                categoryRepository.save(oldCategory);
                log.info("[PostProcess] oldCategoryId={} decremented", oldCategoryId);
            }

            newCategory.increaseCount(1);
            categoryRepository.save(newCategory);
            log.info("[PostProcess] newCategoryId={} incremented", newCategoryId);
        }

        /* Entity 최신화 */
        file.setCategoryId(newCategoryId);
        file.setContext(analyzeResponse.getDescription());
        file.setOcrText(ocrText);
        file.setVectorId(fileEmbeddings.getId());

        sw.start("OpenSearch");
        String categoryName = categoryRepository.findById(newCategoryId)
                .map(Category::getCategoryName)
                .orElse("미분류");

        indexToOpenSearchAsync(file, categoryName);
        sw.stop();

        log.info(sw.prettyPrint());
        log.info("[PostProcess] total={} ms", sw.getTotalTimeMillis());

        return file;
    }

    /**
     * OpenSearch 색인 (비동기 처리)
     * 파일 저장/수정 시 자동으로 검색 인덱스 업데이트
     */
    @Async
    public void indexToOpenSearchAsync(File file, String categoryName) {
        try {
            // 현재 저장된 태그 조회
            List<String> tagNames = tagRepository.findAllByFileId(file.getId())
                    .stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            FileSearch fileSearch = FileSearch.builder()
                    .fileId(file.getId())
                    .userId(file.getUserId())
                    .categoryName(categoryName)
                    .tags(String.join(", ", tagNames))
                    .context(file.getContext() != null ? file.getContext() : "")
                    .ocrText(file.getOcrText() != null ? file.getOcrText() : "")
                    .imageUrl(file.getSrc())
                    .createdAt(file.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .favorite(file.getFavorite() != null ? file.getFavorite() : false)
                    .fileType(file.getType())
                    .build();

            fileSearchService.save(fileSearch);
            log.info(" OpenSearch 색인 완료: fileId={}", file.getId());

        } catch (Exception e) {
            log.error(" OpenSearch 색인 실패: fileId={}", file.getId(), e);
            // 실패해도 파일은 PostgreSQL에 저장되어 있음
        }
    }

    /**
     * OpenSearch 인덱스 삭제 (비동기 처리)
     * 파일 삭제 시 자동으로 검색 인덱스에서 제거
     */
    @Async
    public void deleteFromOpenSearchAsync(Long fileId) {
        try {
            fileSearchService.delete(fileId);
            log.info(" OpenSearch 인덱스 삭제 완료: fileId={}", fileId);

        } catch (Exception e) {
            log.error(" OpenSearch 인덱스 삭제 실패: fileId={}", fileId, e);
            // 실패해도 파일은 PostgreSQL에서 삭제되어 있음
        }
    }

}

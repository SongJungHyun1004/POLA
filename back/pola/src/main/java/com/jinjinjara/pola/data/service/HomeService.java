package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.data.dto.response.CategorySection;
import com.jinjinjara.pola.data.dto.response.DataResponse;
import com.jinjinjara.pola.data.dto.response.HomeResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final FileRepository fileRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service; //  추가

    public HomeResponse getHomeData(Long userId) {
        // --- 카테고리별 파일 ---
        List<Category> categories = categoryRepository.findAllByUserId(userId);

        // 먼저 모든 파일을 모아두고, 한 번에 presigned URL 생성 (효율성 ↑)
        List<File> allFiles = new ArrayList<>();

        // 카테고리별 최근 5개
        Map<Long, List<File>> categoryFileMap = new HashMap<>();
        for (Category c : categories) {
            List<File> files = fileRepository.findTop5ByUserIdAndCategoryIdOrderByCreatedAtDesc(userId, c.getId());
            categoryFileMap.put(c.getId(), files);
            allFiles.addAll(files);
        }

        // 즐겨찾기 3개
        List<File> favorites = fileRepository.findTop3ByUserIdAndFavoriteTrueOrderByCreatedAtDesc(userId);
        allFiles.addAll(favorites);

        // 리마인드 3개 (7일 이상 안 본 파일)
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<File> reminds = fileRepository.findRemindPreview(userId, sevenDaysAgo, PageRequest.of(0, 3));
        allFiles.addAll(reminds);

        // 타임라인 최근 10개
        List<File> timeline = fileRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        allFiles.addAll(timeline);

        // --- Presigned URL 생성 (한 번에 처리) ---
        Map<Long, S3Service.FileMeta> fileMetaMap = allFiles.stream()
                .filter(f -> f.getSrc() != null)
                .collect(Collectors.toMap(
                        File::getId,
                        f -> new S3Service.FileMeta(f.getSrc(), f.getType()),
                        (a, b) -> a
                ));

        Map<Long, String> previewUrls = s3Service.generatePreviewUrls(fileMetaMap);

        // --- DTO 변환 ---
        List<CategorySection> categorySections = categories.stream()
                .map(c -> CategorySection.builder()
                        .categoryId(c.getId())
                        .categoryName(c.getCategoryName())
                        .files(
                                categoryFileMap.getOrDefault(c.getId(), List.of())
                                        .stream()
                                        .map(f -> toDataResponse(f, previewUrls))
                                        .toList()
                        )
                        .build())
                .toList();

        List<DataResponse> favoriteDtos = favorites.stream()
                .map(f -> toDataResponse(f, previewUrls))
                .toList();

        List<DataResponse> remindDtos = reminds.stream()
                .map(f -> toDataResponse(f, previewUrls))
                .toList();

        List<DataResponse> timelineDtos = timeline.stream()
                .map(f -> toDataResponse(f, previewUrls))
                .toList();

        // --- 응답 빌드 ---
        return HomeResponse.builder()
                .categories(categorySections)
                .favorites(favoriteDtos)
                .reminds(remindDtos)
                .timeline(timelineDtos)
                .build();
    }

    /**
     *  File → DataResponse 변환 (Presigned URL 주입)
     */
    private DataResponse toDataResponse(File f, Map<Long, String> previewUrls) {
        return DataResponse.builder()
                .id(f.getId())
                .src(previewUrls.get(f.getId())) // 접근 가능한 URL
                .type(f.getType())
                .ocrText(f.getOcrText())
                .context(f.getContext())
                .favorite(f.getFavorite())
                .build();
    }
}

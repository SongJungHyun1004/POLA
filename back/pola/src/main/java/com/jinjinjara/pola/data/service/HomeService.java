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
    private final S3Service s3Service;

    public HomeResponse getHomeData(Long userId) {

        // 3. 카테고리를 파일 개수 기준으로 정렬해서 가져오기
        List<Category> categories = categoryRepository.findAllSorted(userId);

        // 5. 파일들 한 번에 모아서 presigned URL 한 번에 생성
        List<File> allFiles = new ArrayList<>();
        Map<Long, List<File>> categoryFileMap = new HashMap<>();

        for (Category c : categories) {
            List<File> files = fileRepository.findTop5ByUserIdAndCategoryIdOrderByCreatedAtDesc(userId, c.getId());
            categoryFileMap.put(c.getId(), files);
            allFiles.addAll(files);
        }

        List<File> favorites = fileRepository.findTop3ByUserIdAndFavoriteTrueOrderByCreatedAtDesc(userId);
        allFiles.addAll(favorites);

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<File> reminds = fileRepository.findRemindPreview(userId, sevenDaysAgo, PageRequest.of(0, 3));
        allFiles.addAll(reminds);

        List<File> timeline = fileRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        allFiles.addAll(timeline);

        // 6. presigned url 생성(한 번에)
        Map<Long, S3Service.FileMeta> fileMetaMap = allFiles.stream()
                .filter(f -> f.getSrc() != null)
                .collect(Collectors.toMap(
                        File::getId,
                        f -> new S3Service.FileMeta(f.getSrc(), f.getType()),
                        (a, b) -> a
                ));

        Map<Long, String> previewUrls = s3Service.generatePreviewUrls(fileMetaMap);

        // 7. DTO 변환
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

        return HomeResponse.builder()
                .categories(categorySections)
                .favorites(favorites.stream().map(f -> toDataResponse(f, previewUrls)).toList())
                .reminds(reminds.stream().map(f -> toDataResponse(f, previewUrls)).toList())
                .timeline(timeline.stream().map(f -> toDataResponse(f, previewUrls)).toList())
                .build();
    }

    private DataResponse toDataResponse(File f, Map<Long, String> previewUrls) {
        return DataResponse.builder()
                .id(f.getId())
                .src(previewUrls.get(f.getId()))
                .type(f.getType())
                .ocrText(f.getOcrText())
                .context(f.getContext())
                .favorite(f.getFavorite())
                .build();
    }
}

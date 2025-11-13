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

        List<Category> categories = categoryRepository.findAllSorted(userId);

        List<File> top5Files = fileRepository.findTop5FilesPerCategory(userId);

        Map<Long, List<File>> categoryFileMap = top5Files.stream()
                .collect(Collectors.groupingBy(File::getCategoryId));

        List<File> allFiles = new ArrayList<>(top5Files);

        List<File> favorites = fileRepository
                .findTop3ByUserIdAndFavoriteTrueOrderByCreatedAtDesc(userId);
        allFiles.addAll(favorites);

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<File> reminds = fileRepository.findRemindPreview(userId, sevenDaysAgo, PageRequest.of(0, 3));
        allFiles.addAll(reminds);

        List<File> timeline = fileRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        allFiles.addAll(timeline);

        Map<Long, S3Service.FileMeta> fileMetaMap = allFiles.stream()
                .filter(f -> f.getSrc() != null)
                .collect(Collectors.toMap(
                        File::getId,
                        f -> new S3Service.FileMeta(f.getSrc(), f.getType()),
                        (a, b) -> a
                ));

        Map<Long, String> previewUrls = s3Service.generatePreviewUrls(fileMetaMap);

        List<CategorySection> categorySections = categories.stream()
                .map(c -> CategorySection.builder()
                        .categoryId(c.getId())
                        .categoryName(c.getCategoryName())
                        .files(
                                categoryFileMap.getOrDefault(c.getId(), List.of()).stream()
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

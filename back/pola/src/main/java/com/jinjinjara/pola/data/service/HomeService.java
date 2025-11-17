package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.data.dto.response.CategorySection;
import com.jinjinjara.pola.data.dto.response.DataResponse;
import com.jinjinjara.pola.data.dto.response.HomeResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.data.repository.RemindCacheRepository;
import com.jinjinjara.pola.s3.service.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class HomeService {

    private final FileRepository fileRepository;
    private final CategoryRepository categoryRepository;
    private final S3Service s3Service;
    private final RemindCacheRepository remindCacheRepository;

    public HomeResponse getHomeData(Long userId) {

        log.info("[Home] start userId={}", userId);

        List<Category> categories = categoryRepository.findAllSorted(userId);
        log.info("[Home] categories size={}", categories.size());

        List<File> top5Files = fileRepository.findTop5FilesPerCategory(userId);
        log.info("[Home] top5Files size={}", top5Files.size());

        Map<Long, List<File>> categoryFileMap = top5Files.stream()
                .collect(Collectors.groupingBy(File::getCategoryId));

        List<File> allFiles = new ArrayList<>(top5Files);

        List<File> favorites =
                Optional.ofNullable(fileRepository
                                .findTop3ByUserIdAndFavoriteTrueOrderByCreatedAtDesc(userId))
                        .orElse(List.of());
        log.info("[Home] favorites size={}", favorites.size());

        allFiles.addAll(favorites);

        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<DataResponse> cachedReminds = remindCacheRepository.getRemindFiles(userId);
        List<File> reminds;

        if (cachedReminds != null) {
            log.info("[Home] Remind Cache HIT size={}", cachedReminds.size());
            reminds = cachedReminds.stream()
                    .map(this::toFileEntity)
                    .toList();
        } else {
            log.info("[Home] Remind Cache MISS");
            reminds = fileRepository.findRemindPreview(
                    userId,
                    sevenDaysAgo,
                    PageRequest.of(0, 3)
            );
            remindCacheRepository.saveRemindFiles(
                    userId,
                    reminds.stream()
                            .map(f -> toDataResponse(f, Map.of()))
                            .toList()
            );
        }

        log.info("[Home] reminds size={}", reminds.size());
        allFiles.addAll(reminds);

        List<File> timeline = fileRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId);
        log.info("[Home] timeline size={}", timeline.size());

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

    private File toFileEntity(DataResponse dto) {
        File file = new File();
        file.setId(dto.getId());
        file.setSrc(dto.getSrc());
        file.setType(dto.getType());
        file.setOcrText(dto.getOcrText());
        file.setContext(dto.getContext());
        file.setFavorite(dto.getFavorite());
        return file;
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

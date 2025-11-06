package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.data.dto.response.CategorySection;
import com.jinjinjara.pola.data.dto.response.DataResponse;
import com.jinjinjara.pola.data.dto.response.HomeResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.FileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class HomeService {

    private final FileRepository fileRepository;
    private final CategoryRepository categoryRepository;

    public HomeResponse getHomeData(Long userId) {
        // --- 카테고리별 파일 ---
        List<Category> categories = categoryRepository.findAllByUserId(userId);
        List<CategorySection> categorySections = categories.stream()
                .map(c -> CategorySection.builder()
                        .categoryId(c.getId())
                        .categoryName(c.getCategoryName())
                        .files(
                                fileRepository.findTop5ByUserIdAndCategoryIdOrderByCreatedAtDesc(userId, c.getId())
                                        .stream()
                                        .map(this::toDataResponse)
                                        .toList()
                        )
                        .build())
                .toList();

        // --- 즐겨찾기 ---
        List<DataResponse> favorites = fileRepository.findTop3ByUserIdAndFavoriteTrueOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDataResponse)
                .toList();

        // --- 리마인드 (7일 이상 안 본 파일) ---
        LocalDateTime sevenDaysAgo = LocalDateTime.now().minusDays(7);
        List<DataResponse> reminds = fileRepository.findRemindPreview(userId, sevenDaysAgo, PageRequest.of(0, 3))
                .stream()
                .map(this::toDataResponse)
                .toList();

        // --- 타임라인 ---
        List<DataResponse> timeline = fileRepository.findTop10ByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toDataResponse)
                .toList();

        return HomeResponse.builder()
                .categories(categorySections)
                .favorites(favorites)
                .reminds(reminds)
                .timeline(timeline)
                .build();
    }

    private DataResponse toDataResponse(File f) {
        return DataResponse.builder()
                .id(f.getId())
                .src(f.getSrc())
                .type(f.getType())
                .context(f.getContext())
                .favorite(f.getFavorite())
                .build();
    }
}

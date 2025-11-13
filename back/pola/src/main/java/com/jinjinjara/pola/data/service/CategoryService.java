package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.auth.repository.UserRepository;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.dto.request.CategoryWithTags;
import com.jinjinjara.pola.data.dto.request.InitCategoryTagRequest;
import com.jinjinjara.pola.data.dto.response.CategoryIdResponse;
import com.jinjinjara.pola.data.dto.response.CategoryResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.CategoryTag;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.CategoryTagRepository;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.data.repository.TagRepository;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.vision.dto.common.CategoryChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.stream.Collectors;
import com.jinjinjara.pola.data.dto.response.RecommendedCategory;
import com.jinjinjara.pola.data.dto.response.RecommendedCategoryList;
import com.jinjinjara.pola.data.entity.Tag;

import java.util.*;
@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CategoryTagRepository categoryTagRepository;
    private final FileRepository fileRepository;
    private final ApplicationEventPublisher publisher;

    /**
     * CREATE - 카테고리 생성
     */
    public CategoryResponse createCategory(Users user, String categoryName) {
        try {
            // 중복 이름 검사
            boolean exists = categoryRepository.existsByUserAndCategoryName(user, categoryName);
            if (exists) {
                throw new CustomException(ErrorCode.CATEGORY_ALREADY_EXISTS);
            }

            Category category = Category.builder()
                    .user(user)
                    .categoryName(categoryName)
                    .categorySort(0)
                    .build();

            Category saved = categoryRepository.save(category);
            publisher.publishEvent(new CategoryChangedEvent(user.getId()));
            return CategoryResponse.fromEntity(saved);
        } catch (CustomException e) {
            throw e; // 그대로 전달
        } catch (Exception e) {
            throw new CustomException(ErrorCode.CATEGORY_CREATE_FAIL, e.getMessage());
        }
    }

    /**
     * READ - 유저 기준 전체 조회
     */
    @Transactional(readOnly = true)
    public List<CategoryResponse> getCategoriesByUser(Users user) {
        try {
            List<Category> categories = categoryRepository.findByUser(user);
            if (categories.isEmpty()) {
                throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
            }
            return categories.stream()
                    .map(CategoryResponse::fromEntity)
                    .toList();
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DATA_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * READ - 단일 조회
     */
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
            return CategoryResponse.fromEntity(category);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DATA_NOT_FOUND, e.getMessage());
        }
    }

    /**
     * UPDATE - 이름 수정
     */
    public CategoryResponse updateCategory(Long id, String newName) {
        try {
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

            // 이름 중복 확인
            boolean exists = categoryRepository.existsByUserAndCategoryName(category.getUser(), newName);
            if (exists) {
                throw new CustomException(ErrorCode.CATEGORY_ALREADY_EXISTS);
            }

            category = Category.builder()
                    .id(category.getId())
                    .user(category.getUser())
                    .categoryName(newName)
                    .categorySort(category.getCategorySort())
                    .createdAt(category.getCreatedAt())
                    .build();

            Category updated = categoryRepository.save(category);
            return CategoryResponse.fromEntity(updated);
        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.CATEGORY_UPDATE_FAIL, e.getMessage());
        }
    }

    /**
     * DELETE
     * 카테고리 삭제 시, 해당 카테고리를 참조하는 파일들은 모두 '미분류' 카테고리로 이동
     */
    @Transactional
    public void deleteCategory(Long id) {
        try {
            // 1. 삭제 대상 카테고리 확인
            Category category = categoryRepository.findById(id)
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

            Users user = category.getUser(); // Category 엔티티의 Users 참조

            // 2. 해당 유저의 '미분류' 카테고리 찾기 (없으면 생성)
            Category uncategorized = categoryRepository.findByUserIdAndCategoryName(user.getId(), "미분류")
                    .orElseGet(() -> {
                        Category newCategory = Category.builder()
                                .user(user) // 올바른 필드 이름
                                .categoryName("미분류")
                                .build();
                        return categoryRepository.save(newCategory);
                    });

            // 3. 연결된 파일들의 category_id를 모두 '미분류'로 변경
            List<File> files = fileRepository.findAllByCategoryId(id);
            for (File file : files) {
                file.setCategoryId(uncategorized.getId());
            }
            fileRepository.saveAll(files);

            // 4. category_tags 먼저 삭제
            categoryTagRepository.deleteByCategoryId(id);

            // 5. 카테고리 삭제
            categoryRepository.deleteById(id);

            // 6. 유저 캐시 삭제
            publisher.publishEvent(new CategoryChangedEvent(user.getId()));

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.CATEGORY_DELETE_FAIL, e.getMessage());
        }
    }







    /**
     * 기본 카테고리별 추천 태그 리스트 반환
     */
    public RecommendedCategoryList getRecommendations() {
        List<RecommendedCategory> recs = List.of(
                new RecommendedCategory("업무", List.of("회의", "보고서", "프로젝트", "결제")),
                new RecommendedCategory("스터디", List.of("공부", "과제", "정리필요", "노트")),
                new RecommendedCategory("아이디어", List.of("영감", "브레인스토밍", "기획", "메모")),
                new RecommendedCategory("사진", List.of("추억", "여행", "가족", "풍경")),
                new RecommendedCategory("영수증", List.of("지출", "정산", "회계", "보관"))
        );

        return RecommendedCategoryList.builder()
                .recommendations(recs)
                .build();
    }

    @Transactional(readOnly = true)
    public CategoryIdResponse findCategoryIdByName(Long userId, String categoryName) {
        try {
            Optional<Long> categoryIdOpt = categoryRepository.findIdByUserIdAndCategoryName(userId, categoryName);
            if (categoryIdOpt.isEmpty()) {
                throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND, "해당 이름의 카테고리를 찾을 수 없습니다: " + categoryName);
            }

            // 카테고리 이름을 함께 내려주기 위해 추가 조회
            Category category = categoryRepository.findById(categoryIdOpt.get())
                    .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

            return CategoryIdResponse.builder()
                    .categoryId(category.getId())
                    .categoryName(category.getCategoryName())
                    .build();

        } catch (CustomException e) {
            throw e;
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DATA_NOT_FOUND, "카테고리 조회 중 오류 발생: " + e.getMessage());
        }
    }
}

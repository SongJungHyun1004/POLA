package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.auth.repository.UserRepository;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.dto.request.CategoryWithTags;
import com.jinjinjara.pola.data.dto.request.InitCategoryTagRequest;
import com.jinjinjara.pola.data.dto.response.CategoryResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.CategoryTag;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.CategoryTagRepository;
import com.jinjinjara.pola.data.repository.TagRepository;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
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
     */
    public void deleteCategory(Long id) {
        try {
            if (!categoryRepository.existsById(id)) {
                throw new CustomException(ErrorCode.CATEGORY_NOT_FOUND);
            }
            categoryRepository.deleteById(id);
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



}

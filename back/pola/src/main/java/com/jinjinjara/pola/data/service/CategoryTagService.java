package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.common.YamlRecommendedCatalogService;
import com.jinjinjara.pola.data.dto.request.CategoryWithTags;
import com.jinjinjara.pola.data.dto.request.InitCategoryTagRequest;
import com.jinjinjara.pola.data.dto.response.*;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.Tag;
import com.jinjinjara.pola.data.entity.CategoryTag;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.TagRepository;
import com.jinjinjara.pola.data.repository.CategoryTagRepository;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.vision.dto.common.CategoryChangedEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryTagService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CategoryTagRepository categoryTagRepository;
    private final YamlRecommendedCatalogService yamlCatalog;
    private final ApplicationEventPublisher publisher;

    // 카테고리에 태그 추가
    public CategoryTagResponse addTagToCategory(Long categoryId, Long tagId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));

        categoryTagRepository.findByCategoryAndTag(category, tag)
                .ifPresent(ct -> {
                    throw new CustomException(ErrorCode.TAG_LINK_DUPLICATE);
                });

        try {
            CategoryTag categoryTag = CategoryTag.builder()
                    .category(category)
                    .tag(tag)
                    .build();

            CategoryTag saved = categoryTagRepository.save(categoryTag);
            publisher.publishEvent(new CategoryChangedEvent(category.getUser().getId()));
            return CategoryTagResponse.fromEntity(saved);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TAG_CREATE_FAIL, e.getMessage());
        }
    }

    // 카테고리에서 태그 제거
    public void removeTagFromCategory(Long categoryId, Long tagId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));

        try {
            categoryTagRepository.deleteByCategoryAndTag(category, tag);
            publisher.publishEvent(new CategoryChangedEvent(category.getUser().getId()));
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DATA_DELETE_FAIL, e.getMessage());
        }
    }

    // 특정 카테고리의 모든 태그 조회
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        List<TagResponse> tags = categoryTagRepository.findByCategory(category)
                .stream()
                .map(CategoryTag::getTag)
                .map(TagResponse::fromEntity)
                .toList();

        if (tags.isEmpty()) {
            throw new CustomException(ErrorCode.TAG_NOT_FOUND);
        }

        return tags;
    }

    // 전체 태그 조회
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        List<TagResponse> tags = tagRepository.findAll()
                .stream()
                .map(TagResponse::fromEntity)
                .toList();

        if (tags.isEmpty()) {
            throw new CustomException(ErrorCode.TAG_NOT_FOUND);
        }

        return tags;
    }

    // 새 태그 생성
    public TagResponse createTag(String tagName) {
        if (tagRepository.findByTagName(tagName).isPresent()) {
            throw new CustomException(ErrorCode.TAG_ALREADY_EXISTS);
        }

        try {
            Tag tag = tagRepository.save(Tag.builder().tagName(tagName).build());
            return TagResponse.fromEntity(tag);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TAG_CREATE_FAIL, e.getMessage());
        }
    }

    // 태그 삭제
    public void deleteTag(Long tagId) {
        if (!tagRepository.existsById(tagId)) {
            throw new CustomException(ErrorCode.TAG_NOT_FOUND);
        }
        try {
            tagRepository.deleteById(tagId);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DATA_DELETE_FAIL, e.getMessage());
        }
    }
    @Transactional
    public void initCategoriesAndTags(Users user, InitCategoryTagRequest request) {
        Set<String> categoryNames = request.getCategories().stream()
                .map(CategoryWithTags::getCategoryName)
                .collect(Collectors.toSet());
        categoryNames.add("미분류");

        // 카테고리 등록
        categoryNames.forEach(name -> {
            if (!categoryRepository.existsByUserAndCategoryName(user, name)) {
                categoryRepository.save(Category.builder()
                        .user(user)
                        .categoryName(name)
                        .build());
            }
        });

        // 카테고리-태그 등록
        request.getCategories().forEach(c -> {
            c.getTags().forEach(tagName -> {
                Tag tag = tagRepository.findByTagName(tagName)
                        .orElseGet(() -> tagRepository.save(Tag.builder()
                                .tagName(tagName)
                                .build()));

                Category category = categoryRepository
                        .findByUserAndCategoryName(user, c.getCategoryName())
                        .orElseThrow(() -> new IllegalArgumentException("카테고리 없음: " + c.getCategoryName()));

                //중복 연결 체크 추가
                boolean exists = categoryTagRepository.existsByCategoryAndTag(category, tag);
                if (!exists) {
                    categoryTagRepository.save(CategoryTag.builder()
                            .category(category)
                            .tag(tag)
                            .build());
                }
            });
        });
        publisher.publishEvent(new CategoryChangedEvent(user.getId()));
    }


    @Transactional(readOnly = true)
    public RecommendedCategoryList getRecommendedCategoriesAndTags() {
        return yamlCatalog.getRecommendedCategories();
    }

    @Transactional(readOnly = true)
    public List<CategoryWithTagsResponse> getUserCategoriesWithTags(Users user) {
        List<CategoryTag> categoryTags = categoryTagRepository.findAllByUserId(user.getId());

        // Category별로 묶기
        Map<Long, List<TagResponse>> categoryToTags = categoryTags.stream()
                .collect(Collectors.groupingBy(
                        ct -> ct.getCategory().getId(),
                        Collectors.mapping(ct -> TagResponse.builder()
                                .id(ct.getTag().getId())
                                .tagName(ct.getTag().getTagName())
                                .build(), Collectors.toList())
                ));

        // 카테고리별 응답 리스트 구성
        return categoryToTags.entrySet().stream()
                .map(entry -> {
                    Category category = categoryTags.stream()
                            .filter(ct -> ct.getCategory().getId().equals(entry.getKey()))
                            .findFirst()
                            .get()
                            .getCategory();

                    return CategoryWithTagsResponse.builder()
                            .categoryId(category.getId())
                            .categoryName(category.getCategoryName())
                            .tags(entry.getValue())
                            .build();
                })
                .toList();
    }
    /**
     * ✅ 카테고리에 여러 태그를 한 번에 추가
     * - 같은 이름의 태그가 없으면 새로 생성
     * - 이미 연결된 태그는 중복 연결 방지
     */
    @Transactional
    public List<CategoryTagResponse> addTagsToCategory(Long categoryId, List<String> tagNames) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new CustomException(ErrorCode.CATEGORY_NOT_FOUND));

        List<CategoryTagResponse> results = tagNames.stream()
                .map(tagName -> {
                    //  태그 존재 확인 (없으면 생성)
                    Tag tag = tagRepository.findByTagName(tagName)
                            .orElseGet(() -> tagRepository.save(Tag.builder().tagName(tagName).build()));

                    // 2이미 연결된 경우 건너뛰기
                    boolean exists = categoryTagRepository.existsByCategoryAndTag(category, tag);
                    if (exists) {
                        System.out.println("[CategoryTagService] 이미 연결된 태그: " + tagName);
                        return null;
                    }

                    //  새로운 연결 저장
                    CategoryTag categoryTag = CategoryTag.builder()
                            .category(category)
                            .tag(tag)
                            .build();
                    CategoryTag saved = categoryTagRepository.save(categoryTag);

                    return CategoryTagResponse.fromEntity(saved);
                })
                .filter(ct -> ct != null)
                .toList();

        publisher.publishEvent(new CategoryChangedEvent(category.getUser().getId()));
        return results;
    }
}

package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.dto.response.CategoryTagResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.Tag;
import com.jinjinjara.pola.data.entity.CategoryTag;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.TagRepository;
import com.jinjinjara.pola.data.repository.CategoryTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryTagService {

    private final CategoryRepository categoryRepository;
    private final TagRepository tagRepository;
    private final CategoryTagRepository categoryTagRepository;

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
}

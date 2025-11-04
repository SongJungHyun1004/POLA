package com.jinjinjara.pola.data.service;

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
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        categoryTagRepository.findByCategoryAndTag(category, tag)
                .ifPresent(ct -> {
                    throw new IllegalStateException("Tag already linked to category");
                });

        CategoryTag categoryTag = CategoryTag.builder()
                .category(category)
                .tag(tag)
                .build();

        return CategoryTagResponse.fromEntity(categoryTagRepository.save(categoryTag));
    }

    // 카테고리에서 태그 제거
    public void removeTagFromCategory(Long categoryId, Long tagId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        categoryTagRepository.deleteByCategoryAndTag(category, tag);
    }

    // 특정 카테고리의 모든 태그 조회
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsByCategory(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        return categoryTagRepository.findByCategory(category)
                .stream()
                .map(CategoryTag::getTag)
                .map(TagResponse::fromEntity)
                .toList();
    }

    // 전체 태그 조회
    @Transactional(readOnly = true)
    public List<TagResponse> getAllTags() {
        return tagRepository.findAll()
                .stream()
                .map(TagResponse::fromEntity)
                .toList();
    }

    // 새 태그 생성
    public TagResponse createTag(String tagName) {
        Tag tag = tagRepository.save(Tag.builder().tagName(tagName).build());
        return TagResponse.fromEntity(tag);
    }

    // 태그 삭제
    public void deleteTag(Long tagId) {
        tagRepository.deleteById(tagId);
    }
}

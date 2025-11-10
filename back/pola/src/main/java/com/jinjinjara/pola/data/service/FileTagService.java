package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.dto.response.FileTagResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.entity.Tag;
import com.jinjinjara.pola.data.entity.FileTag;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.data.repository.TagRepository;
import com.jinjinjara.pola.data.repository.FileTagRepository;
import com.jinjinjara.pola.search.model.FileSearch;
import com.jinjinjara.pola.search.service.FileSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileTagService {

    private final FileRepository fileRepository;
    private final TagRepository tagRepository;
    private final FileTagRepository fileTagRepository;
    private final CategoryRepository categoryRepository; // ← 추가
    private final FileSearchService fileSearchService;   // ← 추가

    /**
     * 파일에 태그 추가
     */
    public FileTagResponse addTagToFile(Long fileId, Long tagId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));

        fileTagRepository.findByFileAndTag(file, tag)
                .ifPresent(ft -> {
                    throw new CustomException(ErrorCode.TAG_LINK_DUPLICATE);
                });

        try {
            FileTag fileTag = FileTag.builder()
                    .file(file)
                    .tag(tag)
                    .build();

            FileTag saved = fileTagRepository.save(fileTag);
            return FileTagResponse.fromEntity(saved);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TAG_CREATE_FAIL, e.getMessage());
        }
    }

    /**
     * 파일에서 태그 제거 + OpenSearch 업데이트
     */
    public void removeTagFromFile(Long fileId, Long tagId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));

        try {
            fileTagRepository.deleteByFileAndTag(file, tag);

            // ✅ OpenSearch 업데이트
            updateOpenSearchTags(fileId);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DATA_DELETE_FAIL, e.getMessage());
        }
    }

    /**
     * OpenSearch 문서의 tags 필드 업데이트
     * (태그 추가/제거 시 자동 호출)
     */
    private void updateOpenSearchTags(Long fileId) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

            // 현재 모든 태그 조회 (수동 + AI 추가 태그 모두)
            List<String> tagNames = tagRepository.findAllByFileId(fileId)
                    .stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            String categoryName = categoryRepository.findById(file.getCategoryId())
                    .map(Category::getCategoryName)
                    .orElse("미분류");

            // OpenSearch 문서 업데이트 (save()는 upsert 역할)
            FileSearch fileSearch = FileSearch.builder()
                    .fileId(file.getId())
                    .userId(file.getUserId())
                    .categoryName(categoryName)
                    .tags(String.join(", ", tagNames))
                    .context(file.getContext() != null ? file.getContext() : "")
                    .ocrText(file.getOcrText() != null ? file.getOcrText() : "")
                    .imageUrl(file.getSrc())
                    .createdAt(file.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .build();

            fileSearchService.save(fileSearch);
            log.info("✅ OpenSearch 태그 업데이트 완료: fileId={}, tags={}", fileId, tagNames);

        } catch (Exception e) {
            log.error("❌ OpenSearch 태그 업데이트 실패: fileId={}", fileId, e);
            // OpenSearch 실패는 무시 (검색만 일시적으로 불가)
        }
    }

    /**
     * 파일에 연결된 모든 태그 조회
     */
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsByFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        List<TagResponse> tags = fileTagRepository.findByFile(file)
                .stream()
                .map(FileTag::getTag)
                .map(TagResponse::fromEntity)
                .toList();

//        if (tags.isEmpty()) {
//            throw new CustomException(ErrorCode.TAG_NOT_FOUND);
//        }

        return tags;
    }


    @Transactional
    public List<FileTagResponse> addTagsToFile(Long fileId, List<String> tagNames) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        List<FileTagResponse> results = new ArrayList<>();

        for (String tagName : tagNames) {
            // 태그가 이미 존재하면 가져오고, 없으면 생성
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder()
                            .tagName(tagName)
                            .build()));

            // 파일-태그 연결이 이미 존재하는지 확인
            boolean exists = fileTagRepository.existsByFileAndTag(file, tag);
            if (exists) {
                continue; // 이미 연결되어 있으면 건너뜀
            }

            // 연결 생성 및 저장
            FileTag fileTag = FileTag.builder()
                    .file(file)
                    .tag(tag)
                    .build();

            FileTag saved = fileTagRepository.save(fileTag);
            results.add(FileTagResponse.fromEntity(saved));
        }

        // ✅ OpenSearch 업데이트
        updateOpenSearchTags(fileId);

        return results;
    }

}

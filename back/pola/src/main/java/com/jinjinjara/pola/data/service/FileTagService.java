package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.dto.response.*;
import com.jinjinjara.pola.data.entity.*;
import com.jinjinjara.pola.data.repository.*;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.search.model.FileSearch;
import com.jinjinjara.pola.search.service.FileSearchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class FileTagService {

    private final FileRepository fileRepository;
    private final TagRepository tagRepository;
    private final FileTagRepository fileTagRepository;
    private final CategoryRepository categoryRepository;
    private final FileSearchService fileSearchService;
    private final S3Service s3Service;

    public FileTagResponse addTagToFile(Long fileId, Long tagId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));

        fileTagRepository.findByFileAndTag(file, tag)
                .ifPresent(ft -> { throw new CustomException(ErrorCode.TAG_LINK_DUPLICATE); });

        try {
            FileTag fileTag = FileTag.builder()
                    .file(file)
                    .tag(tag)
                    .build();

            FileTag saved = fileTagRepository.save(fileTag);

            // OpenSearch 업데이트 추가
            updateOpenSearchTags(fileId);

            return FileTagResponse.fromEntity(saved);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.TAG_CREATE_FAIL, e.getMessage());
        }
    }

    public void removeTagFromFile(Long fileId, Long tagId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));

        try {
            fileTagRepository.deleteByFileAndTag(file, tag);
            updateOpenSearchTags(fileId);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.DATA_DELETE_FAIL, e.getMessage());
        }
    }

    private void updateOpenSearchTags(Long fileId) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

            List<String> tagNames = tagRepository.findAllByFileId(fileId)
                    .stream()
                    .map(Tag::getTagName)
                    .collect(Collectors.toList());

            String categoryName = categoryRepository.findById(file.getCategoryId())
                    .map(Category::getCategoryName)
                    .orElse("미분류");

            FileSearch fileSearch = FileSearch.builder()
                    .fileId(file.getId())
                    .userId(file.getUserId())
                    .categoryName(categoryName)
                    .tags(String.join(", ", tagNames))
                    .context(file.getContext() != null ? file.getContext() : "")
                    .ocrText(file.getOcrText() != null ? file.getOcrText() : "")
                    .imageUrl(file.getSrc())
                    .createdAt(file.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .favorite(file.getFavorite() != null ? file.getFavorite() : false)
                    .fileType(file.getType())
                    .build();

            fileSearchService.save(fileSearch);
            log.info("OpenSearch 태그 업데이트 완료: fileId={}, tags={}", fileId, tagNames);

        } catch (Exception e) {
            log.error("OpenSearch 태그 업데이트 실패: fileId={}", fileId, e);
        }
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTagsByFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        return fileTagRepository.findByFile(file)
                .stream()
                .map(FileTag::getTag)
                .map(TagResponse::fromEntity)
                .toList();
    }

    @Transactional
    public List<FileTagResponse> addTagsToFile(Long fileId, List<String> tagNames) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_NOT_FOUND));

        List<FileTagResponse> results = new ArrayList<>();

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().tagName(tagName).build()));

            if (fileTagRepository.existsByFileAndTag(file, tag)) {
                continue;
            }

            FileTag fileTag = FileTag.builder()
                    .file(file)
                    .tag(tag)
                    .build();

            FileTag saved = fileTagRepository.save(fileTag);
            results.add(FileTagResponse.fromEntity(saved));
        }

        updateOpenSearchTags(fileId);
        return results;
    }

    @Transactional(readOnly = true)
    public List<TagLatestFileResponse> getTagsWithLatestFiles(Long categoryId) {
        List<TagWithLatestFileDto> tagStats = fileTagRepository.findTagStatsByCategory(categoryId);
        if (tagStats.isEmpty()) {
            return List.of();
        }

        Map<Long, File> latestFiles = tagStats.stream()
                .collect(Collectors.toMap(
                        TagWithLatestFileDto::getTagId,
                        stat -> fileTagRepository.findFirstByFile_CategoryIdAndTag_IdOrderByFile_CreatedAtDescFile_IdDesc(categoryId, stat.getTagId())
                                .map(FileTag::getFile)
                                .orElse(null)

                ));


        Map<Long, String> presignedUrls = latestFiles.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        File::getId,
                        f -> s3Service.generatePreviewUrl(f.getSrc(), f.getType()).toString(),
                        (existing, replacement) -> existing
                ));


        return tagStats.stream()
                .map(stat -> {
                    File file = latestFiles.get(stat.getTagId());
                    return TagLatestFileResponse.builder()
                            .tagId(stat.getTagId())
                            .tagName(stat.getTagName())
                            .fileCount(stat.getFileCount())
                            .latestFile(file == null ? null :
                                    DataResponse.builder()
                                            .id(file.getId())
                                            .src(presignedUrls.get(file.getId()))
                                            .type(file.getType())
                                            .context(file.getContext())
                                            .favorite(file.getFavorite())
                                            .build())
                            .build();
                })
                .toList();
    }
}

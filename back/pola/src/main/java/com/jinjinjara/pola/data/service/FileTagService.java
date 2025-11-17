package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.dto.response.*;
import com.jinjinjara.pola.data.entity.*;
import com.jinjinjara.pola.data.repository.*;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.search.model.FileSearch;
import com.jinjinjara.pola.search.service.FileSearchService;
import com.jinjinjara.pola.user.entity.Users;
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

    private File validateFileOwner(Long fileId, Users user) {
        return fileRepository.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_ACCESS_DENIED));
    }

    public void removeTagFromFile(Long fileId, Long tagId, Users user) {
        File file = validateFileOwner(fileId, user);
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));
        fileTagRepository.deleteByFileAndTag(file, tag);
        updateOpenSearchTags(file);
    }

    @Transactional(readOnly = true)
    public List<TagResponse> getTagsByFile(Long fileId, Users user) {
        File file = validateFileOwner(fileId, user);
        return fileTagRepository.findByFile(file)
                .stream()
                .map(FileTag::getTag)
                .map(TagResponse::fromEntity)
                .toList();
    }



    @Transactional
    public FileTagResponse addTagToFile(Long fileId, Long tagId, Users user) {
        File file = fileRepository.findByIdAndUserId(fileId, user.getId())
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_ACCESS_DENIED));

        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new CustomException(ErrorCode.TAG_NOT_FOUND));

        if (fileTagRepository.existsByFileAndTag(file, tag)) {
            throw new CustomException(ErrorCode.TAG_LINK_DUPLICATE);
        }

        FileTag saved = fileTagRepository.save(
                FileTag.builder()
                        .file(file)
                        .tag(tag)
                        .build()
        );

        updateOpenSearchTags(file); // ⚠ 여기 인자는 File 객체야 (Long 아님)

        return FileTagResponse.fromEntity(saved);
    }


    public List<FileTagResponse> addTagsToFile(Long fileId, List<String> tagNames, Users user) {
        File file = validateFileOwner(fileId, user);
        List<FileTagResponse> results = new ArrayList<>();

        for (String tagName : tagNames) {
            Tag tag = tagRepository.findByTagName(tagName)
                    .orElseGet(() -> tagRepository.save(Tag.builder().tagName(tagName).build()));
            if (fileTagRepository.existsByFileAndTag(file, tag)) continue;

            FileTag saved = fileTagRepository.save(FileTag.builder()
                    .file(file)
                    .tag(tag)
                    .build());

            results.add(FileTagResponse.fromEntity(saved));
        }

        updateOpenSearchTags(file);
        return results;
    }

    @Transactional(readOnly = true)
    public List<TagLatestFileResponse> getTagsWithLatestFiles(Long categoryId, Users user) {
        categoryRepository.findById(categoryId)
                .filter(c -> c.getUser().getId().equals(user.getId()))
                .orElseThrow(() -> new CustomException(ErrorCode.FILE_ACCESS_DENIED));

        List<TagWithLatestFileDto> stats = fileTagRepository.findTagStatsByCategory(categoryId);
        if (stats.isEmpty()) return List.of();

        Map<Long, File> latestFiles = stats.stream()
                .collect(Collectors.toMap(
                        TagWithLatestFileDto::getTagId,
                        stat -> fileTagRepository
                                .findFirstByFile_CategoryIdAndTag_IdOrderByFile_CreatedAtDescFile_IdDesc(categoryId, stat.getTagId())
                                .map(FileTag::getFile)
                                .orElse(null)
                ));

        Map<Long, String> previewUrls = latestFiles.values().stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toMap(
                        File::getId,
                        f -> s3Service.generatePreviewUrl(f.getSrc(), f.getType()).toString()
                ));

        return stats.stream()
                .map(stat -> {
                    File f = latestFiles.get(stat.getTagId());
                    return TagLatestFileResponse.builder()
                            .tagId(stat.getTagId())
                            .tagName(stat.getTagName())
                            .fileCount(stat.getFileCount())
                            .latestFile(f == null ? null :
                                    DataResponse.builder()
                                            .id(f.getId())
                                            .src(previewUrls.get(f.getId()))
                                            .type(f.getType())
                                            .context(f.getContext())
                                            .favorite(f.getFavorite())
                                            .build())
                            .build();
                })
                .toList();
    }

    private void updateOpenSearchTags(File file) {
        try {
            List<String> tagNames = tagRepository.findAllByFileId(file.getId())
                    .stream()
                    .map(Tag::getTagName)
                    .toList();

            String categoryName = categoryRepository.findById(file.getCategoryId())
                    .map(Category::getCategoryName)
                    .orElse("미분류");

            FileSearch fs = FileSearch.builder()
                    .fileId(file.getId())
                    .userId(file.getUserId())
                    .categoryName(categoryName)
                    .tags(String.join(", ", tagNames))
                    .context(Optional.ofNullable(file.getContext()).orElse(""))
                    .ocrText(Optional.ofNullable(file.getOcrText()).orElse(""))
                    .imageUrl(file.getSrc())
                    .createdAt(file.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME))
                    .favorite(Optional.ofNullable(file.getFavorite()).orElse(false))
                    .fileType(file.getType())
                    .build();

            fileSearchService.save(fs);

        } catch (Exception e) {
            log.error("[OpenSearch] 태그 업데이트 실패: fileId={}", file.getId(), e);
        }
    }
}

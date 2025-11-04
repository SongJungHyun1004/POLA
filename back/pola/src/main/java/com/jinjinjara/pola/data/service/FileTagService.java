package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.data.dto.response.FileTagResponse;
import com.jinjinjara.pola.data.dto.response.TagResponse;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.entity.Tag;
import com.jinjinjara.pola.data.entity.FileTag;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.data.repository.TagRepository;
import com.jinjinjara.pola.data.repository.FileTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class FileTagService {

    private final FileRepository fileRepository;
    private final TagRepository tagRepository;
    private final FileTagRepository fileTagRepository;

    // 파일에 태그 추가
    public FileTagResponse addTagToFile(Long fileId, Long tagId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        fileTagRepository.findByFileAndTag(file, tag)
                .ifPresent(ft -> {
                    throw new IllegalStateException("Tag already linked to file");
                });

        FileTag fileTag = FileTag.builder()
                .file(file)
                .tag(tag)
                .build();

        return FileTagResponse.fromEntity(fileTagRepository.save(fileTag));
    }

    // 파일에서 태그 제거
    public void removeTagFromFile(Long fileId, Long tagId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));
        Tag tag = tagRepository.findById(tagId)
                .orElseThrow(() -> new IllegalArgumentException("Tag not found"));

        fileTagRepository.deleteByFileAndTag(file, tag);
    }

    // 파일에 연결된 모든 태그 조회
    @Transactional(readOnly = true)
    public List<TagResponse> getTagsByFile(Long fileId) {
        File file = fileRepository.findById(fileId)
                .orElseThrow(() -> new IllegalArgumentException("File not found"));

        return fileTagRepository.findByFile(file)
                .stream()
                .map(FileTag::getTag)
                .map(TagResponse::fromEntity)
                .toList();
    }
}

package com.jinjinjara.pola.search.service;

import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.data.entity.Category;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.entity.Tag;
import com.jinjinjara.pola.data.repository.CategoryRepository;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.data.repository.TagRepository;
import com.jinjinjara.pola.search.model.FileSearch;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

/**
 * AI 팀원용 OpenSearch 업데이트 서비스
 * Vision API로 태그/OCR 추출 후 이 서비스를 사용하면 OpenSearch에 자동 반영됩니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileSearchUpdateService {

    private final FileRepository fileRepository;
    private final TagRepository tagRepository;
    private final CategoryRepository categoryRepository;
    private final FileSearchService fileSearchService;

    /**
     * AI 팀원용: 파일 ID로 OpenSearch 문서 업데이트
     * Vision API 처리 후 이 메서드만 호출하면 자동으로 검색에 반영됨
     *
     * @param fileId 업데이트할 파일 ID
     */
    @Transactional(readOnly = true)
    public void updateFileSearch(Long fileId) {
        try {
            File file = fileRepository.findById(fileId)
                    .orElseThrow(() -> new IllegalArgumentException("파일을 찾을 수 없습니다: " + fileId));

            // 현재 태그 조회 (수동 + AI 추가 태그)
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
            log.info(" OpenSearch 업데이트 완료: fileId={}", fileId);

        } catch (Exception e) {
            log.error(" OpenSearch 업데이트 실패: fileId={}", fileId, e);
            throw new CustomException(ErrorCode.SEARCH_UPDATE_FAIL, e.getMessage());
        }
    }
}

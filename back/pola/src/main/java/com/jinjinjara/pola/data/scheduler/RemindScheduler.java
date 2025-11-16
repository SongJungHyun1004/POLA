package com.jinjinjara.pola.data.scheduler;

import com.jinjinjara.pola.auth.repository.UserRepository;
import com.jinjinjara.pola.data.dto.response.DataResponse;
import com.jinjinjara.pola.data.entity.File;
import com.jinjinjara.pola.data.entity.FileTag;
import com.jinjinjara.pola.data.repository.FileRepository;
import com.jinjinjara.pola.data.repository.FileTagRepository;
import com.jinjinjara.pola.data.repository.RemindCacheRepository;
import com.jinjinjara.pola.s3.service.S3Service;
import com.jinjinjara.pola.user.entity.Users;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class RemindScheduler {

    private final FileRepository fileRepository;
    private final FileTagRepository fileTagRepository;
    private final S3Service s3Service;
    private final RemindCacheRepository remindCacheRepository;
    private final UserRepository userRepository;


    @Scheduled(cron = "* * 03 * * *", zone = "Asia/Seoul")
    @Transactional
    public void updateAllUsersRemindFiles() {
        log.info("[Scheduler] Remind files update started");

        List<Users> users = userRepository.findAll();

        for (Users u : users) {
            try {
                List<DataResponse> remindList = buildRemindFiles(u.getId());
                remindCacheRepository.saveRemindFiles(u.getId(), remindList);
            } catch (Exception e) {
                log.error("[Scheduler] Failed to update remind for userId={}", u.getId(), e);
            }
        }

        log.info("[Scheduler] Remind update completed");
    }

    // ----------------------------
    //  조회수 낮은 순으로 리마인드 생성
    // ----------------------------
    @Transactional(readOnly = true)
    public List<DataResponse> buildRemindFiles(Long userId) {

        List<File> files = fileRepository.findLeastViewedFiles(
                userId,
                PageRequest.of(0, 30)
        );

        if (files.isEmpty()) return List.of();

        // presigned URL 생성
        Map<Long, S3Service.FileMeta> metaMap = files.stream()
                .collect(Collectors.toMap(
                        File::getId,
                        f -> new S3Service.FileMeta(f.getSrc(), f.getType())
                ));

        Map<Long, String> previewUrls = s3Service.generatePreviewUrlsLongTTL(metaMap);

        // 파일별 태그 조회
        List<Long> fileIds = files.stream().map(File::getId).toList();

        List<FileTag> fileTags = fileTagRepository.findAllByFileIds(fileIds);

        Map<Long, List<String>> tagMap = fileTags.stream()
                .collect(Collectors.groupingBy(
                        ft -> ft.getFile().getId(),
                        Collectors.mapping(ft -> ft.getTag().getTagName(), Collectors.toList())
                ));

        // DataResponse 변환
        return files.stream()
                .map(file -> DataResponse.builder()
                        .id(file.getId())
                        .src(previewUrls.get(file.getId()))
                        .type(file.getType())
                        .context(file.getContext())
                        .ocrText(file.getOcrText())
                        .createdAt(file.getCreatedAt())
                        .favorite(file.getFavorite())
                        .tags(tagMap.getOrDefault(file.getId(), List.of()))
                        .build())
                .toList();
    }
}

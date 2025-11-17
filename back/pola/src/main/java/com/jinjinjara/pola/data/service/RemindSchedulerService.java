package com.jinjinjara.pola.data.service;

import com.jinjinjara.pola.data.dto.response.DataResponse;
import com.jinjinjara.pola.data.repository.RemindCacheRepository;
import com.jinjinjara.pola.data.scheduler.RemindScheduler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class RemindSchedulerService {

    private final RemindScheduler remindScheduler;
    private final RemindCacheRepository remindCacheRepository;

    /**
     * 특정 유저의 리마인드 캐시를 강제로 생성
     */
    public void runSchedulerForUser(Long userId) {
        log.info("[Dev] Running RemindScheduler manually for userId={}", userId);

        var list = remindScheduler.buildRemindFiles(userId);

        remindCacheRepository.saveRemindFiles(userId, list);

        log.info("[Dev] Remind cache updated manually for userId={}, size={}",
                userId, list.size());
    }

    /**
     * 모든 유저에 대해 스케줄러 강제 실행
     */
    public void runSchedulerForAllUsers() {
        log.info("[Dev] Running RemindScheduler manually for all users");

        remindScheduler.updateAllUsersRemindFiles();

        log.info("[Dev] Done running scheduler for all users");
    }
}


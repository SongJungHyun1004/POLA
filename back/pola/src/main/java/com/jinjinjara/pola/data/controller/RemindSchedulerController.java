package com.jinjinjara.pola.data.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.service.RemindSchedulerService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/dev/scheduler")
@RequiredArgsConstructor
public class RemindSchedulerController {

    private final RemindSchedulerService schedulerService;

    /**
     * 특정 유저 리마인드 캐시 강제 생성
     */
    @PostMapping("/remind/{userId}")
    public ApiResponse<String> runRemindForUser(@PathVariable Long userId) {
        schedulerService.runSchedulerForUser(userId);

        return ApiResponse.ok("OK", "Manual remind scheduler executed for userId=" + userId);
    }

    /**
     * 모든 유저 캐시 강제 생성
     */
    @PostMapping("/remind/all")
    public ApiResponse<String> runRemindForAllUsers() {
        schedulerService.runSchedulerForAllUsers();

        return ApiResponse.ok("OK", "Manual remind scheduler executed for ALL users");
    }
}


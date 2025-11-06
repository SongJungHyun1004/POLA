package com.jinjinjara.pola.data.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.data.dto.response.HomeResponse;
import com.jinjinjara.pola.data.service.HomeService;
import com.jinjinjara.pola.user.entity.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Home API", description = "홈 화면 데이터 조회 API")
@RestController
@RequestMapping("/api/v1/users/me/home")
@RequiredArgsConstructor
public class HomeController {

    private final HomeService homeService;

    /**
     * 홈 화면 데이터 조회
     *
     * - 카테고리별 최근 5개 파일
     * - 즐겨찾기 최근 3개
     * - 리마인드 최근 3개 (7일 이상 미열람)
     * - 타임라인 최근 10개
     */
    @Operation(
            summary = "홈 화면 데이터 조회",
            description = """
            사용자의 홈 화면에 표시할 데이터를 한 번에 조회합니다.  
            - 각 카테고리별로 최근 5개 파일  
            - 즐겨찾기(favorite=true) 최근 3개  
            - 리마인드(최근 7일간 열람 X) 3개  
            - 타임라인(전체 최신 파일) 10개  
            모든 섹션은 `createdAt DESC` 기준으로 정렬됩니다.
            """
    )
    @GetMapping
    public ApiResponse<HomeResponse> getHomeData(@AuthenticationPrincipal Users user) {
        HomeResponse response = homeService.getHomeData(user.getId());
        return ApiResponse.ok(response, "홈 화면 데이터 조회 성공");
    }
}

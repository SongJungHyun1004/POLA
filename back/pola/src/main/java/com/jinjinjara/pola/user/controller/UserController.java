package com.jinjinjara.pola.user.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.user.dto.response.UserInfoResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Tag(name = "User API", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/user")
@RequiredArgsConstructor
public class UserController {

    @Operation(summary = "사용자 정보 조회", description = "로그인한 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getUserInfo() {
        return ApiResponse.ok(
                new UserInfoResponse(
                        1L,
                        "user@google.com",
                        "PolaUser",
                        "https://...",
                        LocalDateTime.parse("2025-10-27T10:00:00")
                ),"사용자 정보 조회에 성공했습니다.");
    }

//    @Operation(summary = "카테고리 정보 조회", description = "로그인한 사용자의 카테고리 정보를 조회합니다.")
//    @GetMapping("/me/categories")
//    public ApiResponse<List<CategoryResponse>> getUserCategories() {
//        List<CategoryResponse> userCategories = new ArrayList<>();
//        userCategories.add(new CategoryResponse(10L, "UI"));
//        userCategories.add(new CategoryResponse(11L, "디자인시스템"));
//        return ApiResponse.ok(userCategories,"카테고리 태그 목록 조회에 성공했습니다.");
//    }
}

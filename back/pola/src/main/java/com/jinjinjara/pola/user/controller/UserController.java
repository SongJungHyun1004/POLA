package com.jinjinjara.pola.user.controller;

import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.user.dto.response.UserInfoResponse;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "User API", description = "사용자 API")
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @Operation(
            summary = "내 정보 조회",
            description = """
                    로그인한 사용자의 프로필 정보를 조회합니다.

                    **인증 방식:**
                    - Authorization 헤더에 JWT Access Token을 Bearer 형식으로 전달해야 합니다.

                    **요청 헤더:**
                    ```
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
                    ```

                    **성공 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "사용자 정보 조회에 성공했습니다.",
                      "data": {
                        "id": 1,
                        "email": "user@example.com",
                        "name": "홍길동",
                        "provider": "GOOGLE",
                        "createdAt": "2025-01-15T10:30:00"
                      }
                    }
                    ```

                    **에러 응답 (401 Unauthorized):**
                    - Access Token이 없거나 유효하지 않은 경우
                    - Access Token이 만료된 경우 → `/api/v1/oauth/reissue` 엔드포인트로 토큰 재발급 필요
                    """,
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/me")
    public ApiResponse<UserInfoResponse> getUserInfo(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user) {
        // @AuthenticationPrincipal을 통해 JWT 토큰을 파싱하여 얻은 Users 객체를 직접 받습니다.
        UserInfoResponse userInfo = userService.getCurrentUserInfo(user);
        return ApiResponse.ok(userInfo, "사용자 정보 조회에 성공했습니다.");
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

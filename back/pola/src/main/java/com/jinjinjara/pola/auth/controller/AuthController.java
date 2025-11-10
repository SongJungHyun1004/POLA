package com.jinjinjara.pola.auth.controller;

import com.jinjinjara.pola.auth.dto.request.GoogleLoginRequest;
import com.jinjinjara.pola.auth.dto.response.AuthResultResponse;
import com.jinjinjara.pola.auth.dto.response.TokenResponse;
import com.jinjinjara.pola.auth.service.CustomUserDetailsService;
import com.jinjinjara.pola.auth.dto.request.SignInRequest;
import com.jinjinjara.pola.auth.dto.request.SignUpRequest;
import com.jinjinjara.pola.auth.service.GoogleAuthService;
import com.jinjinjara.pola.common.ApiResponse;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.user.entity.Users;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Auth API", description = "인증 API")
@RestController
@RequestMapping("/api/v1/oauth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomUserDetailsService userService;
    private final GoogleAuthService googleAuthService;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    @Operation(
            summary = "회원가입",
            description = """
                    이메일과 비밀번호로 회원가입을 진행합니다.

                    **요청 본문 예시:**
                    ```json
                    {
                      "email": "user@example.com",
                      "password": "password123",
                      "name": "홍길동"
                    }
                    ```

                    **성공 응답 (201 Created):**
                    - 회원가입이 완료되면 로그인을 진행해야 합니다.
                    """
    )
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<List<Object>>> signup(@RequestBody SignUpRequest signUpRequest) {
        userService.signup(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.okMessage("회원가입에 성공했습니다."));
    }

    @Operation(
            summary = "이메일/비밀번호 로그인",
            description = """
                    이메일과 비밀번호로 로그인합니다.

                    **참고**: 이 로그인 방식은 테스트용이며, 실제로는 Google OAuth2 로그인을 사용합니다.

                    **요청 본문 예시:**
                    ```json
                    {
                      "email": "user@example.com",
                      "password": "password123"
                    }
                    ```

                    **성공 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "로그인에 성공했습니다.",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
                      }
                    }
                    ```
                    """
    )
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<TokenResponse>> signin(@RequestBody SignInRequest signInRequest) {
        TokenResponse token = userService.signIn(signInRequest);
        return ResponseEntity.ok(ApiResponse.ok(token, "로그인에 성공했습니다."));
    }

    @Operation(
            summary = "Google ID Token 로그인",
            description = """
                    Google OAuth2로 발급받은 ID Token으로 로그인합니다.

                    **동작 방식:**
                    1. Google에서 발급받은 ID Token 검증
                    2. 신규 유저인 경우 자동 회원가입 후 로그인 처리 (201 Created)
                    3. 기존 유저인 경우 바로 로그인 처리 (200 OK)

                    **X-Client-Type 헤더에 따른 응답 분기:**
                    - `WEB`: Refresh Token은 HttpOnly 쿠키로, Access Token은 응답 본문으로 전달
                    - `APP` (기본값): Access Token + Refresh Token 모두 응답 본문으로 전달

                    **요청 헤더:**
                    - `X-Client-Type`: WEB | APP (기본값: APP)

                    **요청 본문 예시:**
                    ```json
                    {
                      "idToken": "eyJhbGciOiJSUzI1NiIsImtpZCI6ImY0..."
                    }
                    ```

                    **신규 유저 WEB 클라이언트 응답 (201 Created):**
                    ```json
                    {
                      "status": "success",
                      "message": "회원가입 및 로그인에 성공했습니다.",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIs..."
                      }
                    }
                    ```
                    + Set-Cookie: refresh_token=...; HttpOnly; Secure

                    **기존 유저 APP 클라이언트 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "로그인에 성공했습니다.",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
                      }
                    }
                    ```
                    """
    )
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<?>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request,
            @Parameter(description = "클라이언트 타입 (WEB: 쿠키 사용, APP: JSON 응답)", example = "APP")
            @RequestHeader(value = "X-Client-Type", defaultValue = "APP") String clientType) throws Exception {

        // 1. 서비스 호출: Google ID 토큰으로 인증 처리 (신규 유저인 경우 가입 처리 포함)
        AuthResultResponse authResult = googleAuthService.authenticate(request.getIdToken());
        TokenResponse token = authResult.getTokenResponse();

        // 2. 신규/기존 유저에 따라 메시지 및 상태 코드 결정
        String message = authResult.isNewUser() ? "회원가입 및 로그인에 성공했습니다." : "로그인에 성공했습니다.";
        HttpStatus status = authResult.isNewUser() ? HttpStatus.CREATED : HttpStatus.OK;

        // 3. 클라이언트 타입에 따라 응답 분기
        if ("WEB".equalsIgnoreCase(clientType)) {
            // WEB: Refresh Token은 HttpOnly 쿠키로, Access Token은 본문으로 전달
            ResponseCookie cookie = createRefreshTokenCookie(token.getRefreshToken());
            ApiResponse<AccessTokenOnlyResponse> apiResponse = ApiResponse.ok(
                    new AccessTokenOnlyResponse(token.getAccessToken()), message);

            return ResponseEntity.status(status)
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(apiResponse);
        } else {
            // APP: Access/Refresh Token 모두 본문으로 전달
            return ResponseEntity.status(status)
                    .body(ApiResponse.ok(token, message));
        }
    }

    @Operation(
            summary = "Access Token 재발급",
            description = """
                    Refresh Token을 사용하여 새로운 Access Token과 Refresh Token을 발급받습니다.

                    **사용 목적**: 자동 로그인 구현 (페이지 새로고침 시)

                    **X-Client-Type 헤더에 따른 Refresh Token 전달 방식:**
                    - `WEB`: 쿠키에서 자동으로 Refresh Token 추출 (refresh_token 쿠키 필수)
                    - `APP`: Authorization 헤더로 Refresh Token 전달 (Bearer {refreshToken} 형식)

                    **WEB 클라이언트 요청:**
                    ```
                    POST /api/v1/oauth/reissue
                    X-Client-Type: WEB
                    Cookie: refresh_token=eyJhbGciOiJIUzI1NiIs...
                    ```

                    **APP 클라이언트 요청:**
                    ```
                    POST /api/v1/oauth/reissue
                    X-Client-Type: APP
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIs...
                    ```

                    **WEB 클라이언트 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "토큰 재발급에 성공했습니다.",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIs..."
                      }
                    }
                    ```
                    + Set-Cookie: refresh_token=...; HttpOnly; Secure (새로운 Refresh Token)

                    **APP 클라이언트 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "토큰 재발급에 성공했습니다.",
                      "data": {
                        "accessToken": "eyJhbGciOiJIUzI1NiIs...",
                        "refreshToken": "eyJhbGciOiJIUzI1NiIs..."
                      }
                    }
                    ```

                    **에러 응답 (401 Unauthorized):**
                    - Refresh Token이 만료되었거나 유효하지 않은 경우
                    - WEB: refresh_token 쿠키가 없는 경우
                    - APP: Authorization 헤더가 없거나 형식이 잘못된 경우
                    """
    )
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<?>> reissue(
            @Parameter(description = "APP 클라이언트용: Refresh Token (Bearer {token} 형식)", example = "Bearer eyJhbGciOiJIUzI1NiIs...")
            @RequestHeader(value = "Authorization", required = false) String authHeader,
            @Parameter(description = "WEB 클라이언트용: Refresh Token 쿠키 (자동 전송)", hidden = true)
            @CookieValue(value = "refresh_token", required = false) String refreshTokenFromCookie,
            @Parameter(description = "클라이언트 타입 (WEB: 쿠키 사용, APP: Authorization 헤더)", example = "WEB")
            @RequestHeader(value = "X-Client-Type", defaultValue = "APP") String clientType) {

        // 1. 클라이언트 타입에 따라 Refresh Token 추출
        String refreshToken = resolveRefreshToken(clientType, refreshTokenFromCookie, authHeader);

        // 2. Refresh Token으로 새로운 토큰 발급
        TokenResponse newToken = userService.reissueToken(refreshToken);
        String message = "토큰 재발급에 성공했습니다.";

        // 3. 클라이언트 타입에 따라 응답 분기
        if ("WEB".equalsIgnoreCase(clientType)) {
            // WEB: 새로운 Refresh Token은 HttpOnly 쿠키로, Access Token은 본문으로 전달
            ResponseCookie cookie = createRefreshTokenCookie(newToken.getRefreshToken());
            ApiResponse<AccessTokenOnlyResponse> apiResponse = ApiResponse.ok(
                    new AccessTokenOnlyResponse(newToken.getAccessToken()), message);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(apiResponse);
        } else {
            // APP: 새로운 Access/Refresh Token 모두 본문으로 전달
            return ResponseEntity.ok(ApiResponse.ok(newToken, message));
        }
    }

    /**
     * 클라이언트 타입에 따라 Refresh Token을 쿠키 또는 Authorization 헤더에서 추출합니다.
     */
    private String resolveRefreshToken(String clientType, String fromCookie, String fromHeader) {
        if ("WEB".equalsIgnoreCase(clientType)) {
            if (fromCookie == null) {
                throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN, "리프레시 토큰 쿠키가 없습니다.");
            }
            return fromCookie;
        } else {
            if (fromHeader == null || !fromHeader.startsWith("Bearer ")) {
                throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN, "Authorization 헤더가 유효하지 않습니다.");
            }
            return fromHeader.substring(7); // "Bearer " 접두사 제거
        }
    }

    @Operation(
            summary = "Access Token 검증 (자동 로그인용)",
            description = """
                    Access Token의 유효성을 확인하고 사용자 정보를 반환합니다.

                    **사용 목적**:
                    - 페이지 새로고침 시 자동 로그인 구현
                    - 프론트엔드에서 localStorage/sessionStorage에 저장된 Access Token 검증

                    **동작 방식:**
                    1. Access Token이 유효하면 → 200 OK + 사용자 정보 반환
                    2. Access Token이 만료/유효하지 않으면 → 401 Unauthorized
                    3. 401 에러 시 프론트엔드에서 `/reissue`로 토큰 재발급

                    **요청 헤더:**
                    ```
                    Authorization: Bearer eyJhbGciOiJIUzI1NiIs... (Access Token)
                    ```

                    **성공 응답 (200 OK):**
                    ```json
                    {
                      "status": "success",
                      "message": "토큰이 유효합니다.",
                      "data": {
                        "valid": true,
                        "userId": 1,
                        "email": "user@example.com"
                      }
                    }
                    ```

                    **에러 응답 (401 Unauthorized):**
                    - Access Token이 만료된 경우
                    - Access Token이 유효하지 않은 경우
                    - Authorization 헤더가 없는 경우

                    **프론트엔드 자동 로그인 플로우:**
                    ```javascript
                    // 1. 페이지 로드 시 Access Token 확인
                    const accessToken = localStorage.getItem('accessToken');

                    // 2. Access Token 검증
                    const response = await fetch('/api/v1/oauth/verify', {
                      headers: { 'Authorization': `Bearer ${accessToken}` }
                    });

                    if (response.ok) {
                      // 3-a. 유효하면 로그인 상태 유지
                      const user = await response.json();
                      setUser(user.data);
                    } else if (response.status === 401) {
                      // 3-b. 만료되었으면 Refresh Token으로 재발급
                      const refreshToken = localStorage.getItem('refreshToken');
                      await fetch('/api/v1/oauth/reissue', {
                        headers: { 'Authorization': `Bearer ${refreshToken}` }
                      });
                    }
                    ```
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
    @GetMapping("/verify")
    public ResponseEntity<ApiResponse<TokenVerifyResponse>> verifyToken(
            @Parameter(hidden = true) @AuthenticationPrincipal Users user) {

        // Spring Security가 이미 토큰을 검증했으므로, 이 메서드에 도달했다면 유효한 토큰임
        TokenVerifyResponse response = new TokenVerifyResponse(
                true,
                user.getId(),
                user.getEmail()
        );

        return ResponseEntity.ok(ApiResponse.ok(response, "토큰이 유효합니다."));
    }

    // ==================== Private Helper Methods ====================

    /**
     * Refresh Token을 담을 HttpOnly, Secure 속성의 쿠키를 생성합니다.
     */
    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .path("/")
                .httpOnly(true)
                .secure(true)
                .maxAge(refreshTokenExpireTime / 1000)
                .build();
    }

    // ==================== Internal DTOs ====================

    /**
     * WEB 클라이언트에게 Access Token만 응답 본문에 담아 반환하기 위한 내부 DTO
     */
    @Getter
    private static class AccessTokenOnlyResponse {
        private String accessToken;

        public AccessTokenOnlyResponse(String accessToken) {
            this.accessToken = accessToken;
        }
    }

    /**
     * Access Token 검증 응답 DTO
     */
    @Getter
    private static class TokenVerifyResponse {
        private boolean valid;
        private Long userId;
        private String email;

        public TokenVerifyResponse(boolean valid, Long userId, String email) {
            this.valid = valid;
            this.userId = userId;
            this.email = email;
        }
    }
}
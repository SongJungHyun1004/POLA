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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "회원가입", description = "...")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<List<Object>>> signup(@RequestBody SignUpRequest signUpRequest) {
        userService.signup(signUpRequest);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.okMessage("회원가입에 성공했습니다."));
    }

    @Operation(summary = "로그인", description = "...")
    @PostMapping("/signin")
    public ResponseEntity<ApiResponse<?>> signin(
            @RequestBody SignInRequest signInRequest,
            @RequestHeader(value = "X-Client-Type", defaultValue = "APP") String clientType) {

        // 1. 서비스 호출: 이메일로 토큰(Access, Refresh) 발급
        TokenResponse token = userService.signIn(signInRequest);
        String message = "로그인에 성공했습니다.";

        // 2. 클라이언트 타입에 따라 응답 분기
        if ("WEB".equalsIgnoreCase(clientType)) {
            // WEB: Refresh Token은 HttpOnly 쿠키로, Access Token은 본문으로 전달
            ResponseCookie cookie = createRefreshTokenCookie(token.getRefreshToken());
            ApiResponse<AccessTokenOnlyResponse> apiResponse = ApiResponse.ok(
                    new AccessTokenOnlyResponse(token.getAccessToken()), message);

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .body(apiResponse);
        } else {
            // APP: Access/Refresh Token 모두 본문으로 전달
            return ResponseEntity.ok(ApiResponse.ok(token, message));
        }
    }

    @Operation(summary = "Google ID Token 로그인", description = "...")
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<?>> loginWithGoogle(
            @Valid @RequestBody GoogleLoginRequest request,
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

    @Operation(summary = "리프레시 토큰 재발급", description = "...")
    @PostMapping("/reissue")
    public ResponseEntity<ApiResponse<?>> reissue(
            @RequestHeader(value = "Authorization", required = false) String authHeader, // APP 클라이언트용
            @CookieValue(value = "refresh_token", required = false) String refreshTokenFromCookie, // WEB 클라이언트용
            @RequestHeader(value = "X-Client-Type", defaultValue = "APP") String clientType) {

        // 1. 컨트롤러 책임: 클라이언트 타입에 따라 Refresh Token 추출
        String refreshToken = resolveRefreshToken(clientType, refreshTokenFromCookie, authHeader);
        
        // 2. 서비스 호출: Refresh Token으로 새로운 토큰 발급
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
     * [컨트롤러 책임]
     * 클라이언트 타입에 따라 Refresh Token을 쿠키 또는 Authorization 헤더에서 추출합니다.
     * @param clientType 'WEB' 또는 'APP'
     * @param fromCookie @CookieValue로 전달받은 쿠키 값
     * @param fromHeader @RequestHeader로 전달받은 헤더 값
     * @return 순수 Refresh Token 문자열
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

    /**
     * [컨트롤러 책임]
     * Refresh Token을 담을 HttpOnly, Secure 속성의 쿠키를 생성합니다.
     * @param refreshToken 쿠키에 담을 Refresh Token 값
     * @return ResponseCookie 객체
     */
    private ResponseCookie createRefreshTokenCookie(String refreshToken) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .path("/")       // 쿠키의 유효 경로를 전체로 설정
                .httpOnly(true) // JavaScript에서 접근 불가
                .secure(true)   // HTTPS 환경에서만 전송
                .maxAge(refreshTokenExpireTime / 1000) // 만료 시간을 초 단위로 설정
                .build();
    }

    /**
     * WEB 클라이언트에게 Access Token만 응답 본문에 담아 반환하기 위한 내부 DTO 클래스
     */
    @Getter
    @AllArgsConstructor
    private static class AccessTokenOnlyResponse {
        private String accessToken;
    }
}
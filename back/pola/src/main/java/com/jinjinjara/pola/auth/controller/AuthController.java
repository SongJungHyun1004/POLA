package com.jinjinjara.pola.auth.controller;

import com.jinjinjara.pola.auth.dto.request.GoogleLoginRequest;
import com.jinjinjara.pola.auth.dto.response.AuthResult;
import com.jinjinjara.pola.auth.dto.response.TokenResponse;
import com.jinjinjara.pola.auth.service.CustomUserDetailsService;
import com.jinjinjara.pola.auth.dto.request.SignInRequest;
import com.jinjinjara.pola.auth.dto.request.SignUpRequest;
import com.jinjinjara.pola.auth.service.GoogleAuthService;
import com.jinjinjara.pola.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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

    @Operation(summary = "회원가입", description = "이메일과 사용자 이름으로 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public ApiResponse<List<Object>> signup(@RequestBody SignUpRequest signUpRequest) {
        userService.signup(signUpRequest);
        return ApiResponse.okMessage("회원가입에 성공했습니다.");
    }

    @Operation(summary = "로그인", description = "이메일로 로그인 후 JWT 토큰을 발급받습니다.")
    @PostMapping("/signin")
    public ApiResponse<TokenResponse> signin(@RequestBody SignInRequest signInRequest) {
        TokenResponse token = userService.signIn(signInRequest);
        return ApiResponse.ok(token, "로그인에 성공했습니다.");
    }

    @Operation(summary = "리프레시 토큰 재발급", description = "리프레시 토큰을 사용해 새로운 Access Token을 발급받습니다.")
    @PostMapping("/reissue")
    public ApiResponse<TokenResponse> reissue(@RequestHeader("Authorization") String refreshToken) {
        TokenResponse newToken = userService.reissueToken(refreshToken);
        return ApiResponse.ok(newToken, "토큰 재발급에 성공했습니다.");
    }

    @Operation(summary = "Google ID Token 로그인", description = "프론트에서 받은 Google ID Token으로 내부 JWT를 발급하고, 신규/기존 회원에 따라 다른 상태코드를 반환합니다.")
    @PostMapping("/token")
    public ResponseEntity<ApiResponse<TokenResponse>> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) throws Exception {
        // 1. GoogleAuthService를 통해 인증 처리 및 신규 유저 여부 확인
        AuthResult authResult = googleAuthService.authenticate(request.getIdToken());

        // 2. 응답 본문 생성 (신규/기존 회원에 따라 메시지 분기 처리)
        ApiResponse<TokenResponse> body = ApiResponse.ok(authResult.getTokenResponse(),
                authResult.isNewUser() ? "회원가입 및 로그인에 성공했습니다." : "로그인에 성공했습니다.");

        // 3. 신규 회원이면 201 Created, 기존 회원이면 200 OK 상태 코드 설정
        HttpStatus status = authResult.isNewUser() ? HttpStatus.CREATED : HttpStatus.OK;

        // 4. ResponseEntity에 응답 본문과 상태 코드를 담아 반환
        return new ResponseEntity<>(body, status);
    }
}

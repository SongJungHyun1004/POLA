package com.jinjinjara.pola.controller;

import com.jinjinjara.pola.auth.dto.request.GoogleLoginRequest;
import com.jinjinjara.pola.auth.service.CustomUserDetailsService;
import com.jinjinjara.pola.auth.dto.request.SignInDto;
import com.jinjinjara.pola.auth.dto.request.SignUpDto;
import com.jinjinjara.pola.auth.dto.response.TokenDto;
import com.jinjinjara.pola.auth.service.GoogleAuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Auth API", description = "인증 API")
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final CustomUserDetailsService userService;
    private final GoogleAuthService googleAuthService;

    @Operation(summary = "회원가입", description = "이메일과 사용자 이름으로 회원가입을 진행합니다.")
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody SignUpDto signUpDto) {
        userService.signup(signUpDto);
        return ResponseEntity.status(HttpStatus.CREATED).body("회원가입이 완료되었습니다.");
    }

    @Operation(summary = "로그인", description = "이메일로 로그인 후 JWT 토큰을 발급받습니다.")
    @PostMapping("/signin")
    public ResponseEntity<TokenDto> signin(@RequestBody SignInDto signInDto) {
        TokenDto token = userService.signIn(signInDto);
        return ResponseEntity.ok(token);
    }

    @Operation(summary = "리프레시 토큰 재발급", description = "리프레시 토큰을 사용해 새로운 Access Token을 발급받습니다.")
    @PostMapping("/reissue")
    public ResponseEntity<String> reissue(@RequestHeader("Authorization") String refreshToken) {
        String parsedToken = userService.resolveRefreshToken(refreshToken);
        return ResponseEntity.ok("유효한 리프레시 토큰: " + parsedToken);
    }

    @Operation(summary = "Google ID Token 로그인", description = "프론트에서 받은 Google ID Token으로 내부 JWT를 발급합니다.")
    @PostMapping("/google")
    public ResponseEntity<TokenDto> loginWithGoogle(@Valid @RequestBody GoogleLoginRequest request) throws Exception {
        TokenDto token = googleAuthService.authenticate(request.getIdToken());
        return ResponseEntity.ok(token);
    }
}

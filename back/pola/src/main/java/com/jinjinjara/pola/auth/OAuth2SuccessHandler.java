package com.jinjinjara.pola.auth;

import com.jinjinjara.pola.auth.dto.common.TokenDto;
import com.jinjinjara.pola.auth.jwt.TokenProvider;
import com.jinjinjara.pola.auth.repository.UserRepository;
import com.jinjinjara.pola.user.entity.Users;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment; // Environment import 추가
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken; // OAuth2AuthenticationToken import 추가
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final Environment env; // Environment 주입

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException {
        // OAuth2 로그인 성공 시 호출되는 핸들러
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        log.info("OAuth2 로그인 성공. 사용자 이메일: {}", email);

        // DB에서 사용자 정보 조회 또는 생성
        Users user = userRepository.findByEmail(email).orElseGet(() -> {
            // 신규 사용자 자동 생성
            String name = oAuth2User.getAttribute("name");
            String picture = oAuth2User.getAttribute("picture");
            String sub = oAuth2User.getAttribute("sub");

            Users newUser = Users.builder()
                    .email(email)
                    .displayName(name)
                    .profileImageUrl(picture)
                    .googleSub(sub)
                    .role("ROLE_USER")
                    .build();
            return userRepository.save(newUser);
        });
        Long userId = user.getId();

        // TokenProvider를 사용하여 JWT 토큰 생성
        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication, userId);
        String accessToken = tokenDto.getAccessToken();
        String refreshToken = tokenDto.getRefreshToken();

        // 클라이언트 등록 ID를 통해 웹/모바일 클라이언트 구분
        String clientRegistrationId = ((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId();
        String redirectUri = "";

        if ("google-web".equals(clientRegistrationId)) {
            redirectUri = env.getProperty("app.oauth2.redirect-uri.web");
        } else if ("google-android".equals(clientRegistrationId)) {
            redirectUri = env.getProperty("app.oauth2.redirect-uri.mobile");
        } else {
            // 기본 리디렉션 또는 에러 처리
            redirectUri = "/login-failure"; // 적절한 기본값 또는 에러 페이지로 설정
        }

        // 클라이언트에게 리디렉션할 URL 생성 (토큰 포함)
        String targetUrl = UriComponentsBuilder.fromUriString(redirectUri)
                .queryParam("accessToken", accessToken)
                .queryParam("refreshToken", refreshToken)
                .build().toUriString();

        // 생성된 URL로 리디렉션
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }
}

package com.jinjinjara.pola.auth.service;

import com.jinjinjara.pola.auth.dto.response.TokenResponse;
import com.jinjinjara.pola.auth.redis.RedisUtil;
import com.jinjinjara.pola.auth.jwt.TokenProvider;
import com.jinjinjara.pola.auth.dto.common.Role;
import com.jinjinjara.pola.auth.dto.request.SignInRequest;
import com.jinjinjara.pola.auth.dto.request.SignUpRequest;
import com.jinjinjara.pola.auth.dto.common.TokenDto;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;

@Slf4j
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UsersRepository usersRepository;
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshTokenExpireTime;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return usersRepository.findByEmail(email)
                .map(this::createUserDetails)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }

    private UserDetails createUserDetails(Users user) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole().toString());

        return new User(
                user.getEmail(),
                "",
                Collections.singleton(grantedAuthority)
        );
    }

    @Transactional
    public void signup(SignUpRequest signUpRequest) {
        if (usersRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new CustomException(ErrorCode.USER_ALREADY_EXISTS);
        }

        Users user = Users.builder()
                .email(signUpRequest.getEmail())
                .displayName(signUpRequest.getUsername())
                .role(Role.ROLE_USER)
                .favoriteSum(0)
                .googleSub(signUpRequest.getEmail())
                .build();

        usersRepository.save(user);
    }

    @Transactional
    public TokenResponse signIn(SignInRequest signInRequest) {
        // 1. 이메일로 유저 조회
        Users user = usersRepository.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        // 2. Spring Security용 Authentication 객체 생성
        var authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()));
        UserDetails principal = new org.springframework.security.core.userdetails.User(user.getEmail(), "", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        // 3. Access/Refresh 토큰 생성
        TokenDto token = tokenProvider.generateTokenDto(authentication, user.getId());
        TokenResponse res = new TokenResponse(
                token.getAccessToken(),
                token.getRefreshToken()
        );

        // 4. Redis에 Refresh Token 저장 (만료 시간과 함께)
        redisUtil.save(user.getEmail(), token.getRefreshToken(), refreshTokenExpireTime);
        log.info("[REDIS] saved refresh for {}: {}...",
                user.getEmail(),
                token.getRefreshToken().substring(0, 16));
        return res;
    }

    /**
     * 전달받은 Refresh Token을 검증하고 새로운 Access/Refresh Token을 발급합니다.
     * 컨트롤러에서 토큰 추출 및 유효성 검증(null 체크)을 마친 후 호출됩니다.
     * @param refreshToken 순수 Refresh Token 문자열 (Bearer 제거된 상태)
     * @return 새로운 토큰 DTO
     */
    @Transactional
    public TokenResponse reissueToken(String refreshToken) {
        // 서비스는 토큰을 재발급하는 핵심 비즈니스 로직에만 집중합니다.
        // (클라이언트 구분, 토큰 추출 등은 컨트롤러의 책임)
        TokenDto tokenDto = tokenProvider.reissueAccessToken(refreshToken);
        return new TokenResponse(tokenDto.getAccessToken(), tokenDto.getRefreshToken());
    }
}

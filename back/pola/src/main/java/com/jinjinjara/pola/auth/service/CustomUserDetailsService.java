package com.jinjinjara.pola.auth.service;

import com.jinjinjara.pola.auth.dto.response.TokenResponse;
import com.jinjinjara.pola.auth.exception.InvalidRefreshTokenException;
import com.jinjinjara.pola.auth.exception.MultipleLoginException;
import com.jinjinjara.pola.auth.redis.RedisUtil;
import com.jinjinjara.pola.auth.jwt.TokenProvider;
import com.jinjinjara.pola.auth.dto.common.Role;
import com.jinjinjara.pola.auth.dto.request.SignInRequest;
import com.jinjinjara.pola.auth.dto.request.SignUpRequest;
import com.jinjinjara.pola.auth.dto.common.TokenDto;
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
                .orElseThrow(() -> new UsernameNotFoundException(email + " -> 데이터베이스에서 찾을 수 없습니다."));
    }

    // DB에 User 값이 존재한다면 UserDetails 객체로 만들어서 리턴
    private UserDetails createUserDetails(Users user) {
        GrantedAuthority grantedAuthority = new SimpleGrantedAuthority(user.getRole().toString());

        // Spring 내부 User
        return new User(
                user.getEmail(),
                "",
                Collections.singleton(grantedAuthority)
        );
    }

    @Transactional
    public void signup(SignUpRequest signUpRequest) {
        if (usersRepository.existsByEmail(signUpRequest.getEmail())) {
            throw new MultipleLoginException("이미 가입되어 있는 유저입니다");
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
        Users user = usersRepository.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일: " + signInRequest.getEmail()));

        var authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()));

        UserDetails principal = new org.springframework.security.core.userdetails.User(user.getEmail(), "", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        TokenDto token = tokenProvider.generateTokenDto(authentication, user.getId());
        TokenResponse res = new TokenResponse(
                token.getAccessToken(),
                token.getRefreshToken()
        );


        redisUtil.save(user.getEmail(), token.getRefreshToken(), refreshTokenExpireTime);
        log.info("[REDIS] saved refresh for {}: {}...",
                user.getEmail(),
                token.getRefreshToken().substring(0, 16));
        return res;
    }

    @Transactional(readOnly = true)
    public String resolveRefreshToken(String refreshToken) {
        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            throw new InvalidRefreshTokenException("리프레시 토큰이 누락되었거나 올바르지 않습니다.");
        }
        return refreshToken.substring(7);
    }
}

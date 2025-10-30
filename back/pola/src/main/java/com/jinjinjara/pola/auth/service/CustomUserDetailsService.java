package com.jinjinjara.pola.auth.service;

import com.jinjinjara.pola.auth.exception.InvalidRefreshTokenException;
import com.jinjinjara.pola.auth.exception.MultipleLoginException;
import com.jinjinjara.pola.auth.redis.RedisUtil;
import com.jinjinjara.pola.auth.jwt.TokenProvider;
import com.jinjinjara.pola.auth.dto.common.Role;
import com.jinjinjara.pola.auth.dto.request.SignInDto;
import com.jinjinjara.pola.auth.dto.request.SignUpDto;
import com.jinjinjara.pola.auth.dto.response.TokenDto;
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
    public void signup(SignUpDto signUpDto) {
        if (usersRepository.existsByEmail(signUpDto.getEmail())) {
            throw new MultipleLoginException("이미 가입되어 있는 유저입니다");
        }

        Users user = Users.builder()
                .email(signUpDto.getEmail())
                .displayName(signUpDto.getUsername())
                .role(Role.ROLE_USER)
                .favoriteSum(0)
                .googleSub(signUpDto.getEmail())
                .build();

        usersRepository.save(user);
    }

    @Transactional
    public TokenDto signIn(SignInDto signInDto) {
        Users user = usersRepository.findByEmail(signInDto.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("존재하지 않는 이메일: " + signInDto.getEmail()));

        var authorities = Collections.singletonList(new SimpleGrantedAuthority(user.getRole().toString()));

        UserDetails principal = new org.springframework.security.core.userdetails.User(user.getEmail(), "", authorities);
        Authentication authentication = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

        redisUtil.save(user.getEmail(), tokenDto.getRefreshToken(), refreshTokenExpireTime);
        log.info("[REDIS] saved refresh for {}: {}...",
                user.getEmail(),
                tokenDto.getRefreshToken().substring(0, 16));
        return tokenDto;
    }

    @Transactional(readOnly = true)
    public String resolveRefreshToken(String refreshToken) {
        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            throw new InvalidRefreshTokenException("리프레시 토큰이 누락되었거나 올바르지 않습니다.");
        }
        return refreshToken.substring(7);
    }
}

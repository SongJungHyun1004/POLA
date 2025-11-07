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
        Users user = usersRepository.findByEmail(signInRequest.getEmail())
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

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

    @Transactional
    public TokenResponse reissueToken(String refreshTokenWithBearer) {
        String refreshToken = resolveRefreshToken(refreshTokenWithBearer);
        TokenDto tokenDto = tokenProvider.reissueAccessToken(refreshToken);
        return new TokenResponse(tokenDto.getAccessToken(), tokenDto.getRefreshToken());
    }

    @Transactional(readOnly = true)
    public String resolveRefreshToken(String refreshToken) {
        if (refreshToken == null || !refreshToken.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN);
        }
        return refreshToken.substring(7);
    }
}

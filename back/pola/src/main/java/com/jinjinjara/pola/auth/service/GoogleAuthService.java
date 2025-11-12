package com.jinjinjara.pola.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.jinjinjara.pola.auth.dto.common.Role;
import com.jinjinjara.pola.auth.dto.response.AuthResultResponse;
import com.jinjinjara.pola.auth.dto.response.TokenResponse;
import com.jinjinjara.pola.auth.oauth.GoogleTokenVerifier;
import com.jinjinjara.pola.auth.redis.RedisUtil;
import com.jinjinjara.pola.auth.jwt.TokenProvider;
import com.jinjinjara.pola.auth.dto.common.TokenDto;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GoogleAuthService {

    private final GoogleTokenVerifier googleTokenVerifier;
    private final TokenProvider tokenProvider;
    private final RedisUtil redisUtil;
    private final UsersRepository usersRepository;

    @Value("${jwt.refresh-token-expire-time}")
    private long refreshExpireMs;

    @Transactional
    public AuthResultResponse authenticate(String idToken) {
        GoogleIdToken.Payload payload;
        try {
            payload = googleTokenVerifier.verify(idToken);
            if (payload == null) {
                throw new CustomException(ErrorCode.INVALID_ID_TOKEN);
            }
        } catch (Exception e) {
            throw new CustomException(ErrorCode.INVALID_ID_TOKEN, e.getMessage());
        }

        String sub = payload.getSubject(); // Google unique id
        String email = payload.getEmail();
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        boolean isNewUser;
        Optional<Users> userOptional = usersRepository.findByEmail(email);
        Users user;

        if (userOptional.isPresent()) {
            user = userOptional.get();
            isNewUser = false; // 기존 회원
        } else {
            // 신규 회원
            Users newUser = Users.builder()
                    .email(email)
                    .displayName(name)
                    .profileImageUrl(picture)
                    .googleSub(sub)
                    .role(Role.ROLE_USER)
                    .build();
            user = usersRepository.save(newUser);
            isNewUser = true;
        }

        var authorities = AuthorityUtils.createAuthorityList(user.getRole().toString());
        var principal   = new User(user.getEmail(), "", authorities);
        var auth        = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        TokenDto token = tokenProvider.generateTokenDto(auth, user.getId());
        TokenResponse tokenResponse = new TokenResponse(
                token.getAccessToken(),
                token.getRefreshToken()
        );

        // Refresh Token 저장 (다중 디바이스 지원)
        redisUtil.saveRefreshToken(token.getRefreshToken(), user.getEmail(), refreshExpireMs);

        return new AuthResultResponse(tokenResponse, isNewUser);
    }
}

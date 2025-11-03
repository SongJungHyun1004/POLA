package com.jinjinjara.pola.auth.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.jinjinjara.pola.auth.dto.response.TokenResponse;
import com.jinjinjara.pola.auth.oauth.GoogleTokenVerifier;
import com.jinjinjara.pola.auth.redis.RedisUtil;
import com.jinjinjara.pola.auth.jwt.TokenProvider;
import com.jinjinjara.pola.auth.dto.common.TokenDto;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.user.repository.UsersRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public TokenResponse authenticate(String idToken) throws Exception {
        GoogleIdToken.Payload payload = googleTokenVerifier.verify(idToken);

        String sub = payload.getSubject(); // Google unique id
        String email = (String) payload.get("email");
        String name = (String) payload.get("name");
        String picture = (String) payload.get("picture");

        Users user = usersRepository.findByEmail(email).orElseGet(() -> {
            Users u = Users.builder()
                    .email(email)
                    .displayName(name)
                    .profileImageUrl(picture)
                    .googleSub(sub)
                    .build();
            return usersRepository.save(u);
        });

        var authorities = AuthorityUtils.createAuthorityList("ROLE_USER");
        var principal   = new User(user.getEmail(), "", authorities);
        var auth        = new UsernamePasswordAuthenticationToken(principal, null, authorities);

        TokenDto token = tokenProvider.generateTokenDto(auth);
        TokenResponse res = new TokenResponse(
                token.getAccessToken(),
                token.getRefreshToken()
        );

        // RefreshToken 저장
        redisUtil.save(user.getEmail(), token.getRefreshToken(), refreshExpireMs);

        return res;
    }
}

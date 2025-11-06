package com.jinjinjara.pola.auth.jwt;

import com.jinjinjara.pola.auth.redis.RedisUtil;
import com.jinjinjara.pola.auth.dto.common.TokenDto;
import com.jinjinjara.pola.common.CustomException;
import com.jinjinjara.pola.common.ErrorCode;
import com.jinjinjara.pola.user.entity.Users;
import com.jinjinjara.pola.user.repository.UsersRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final String USER_ID_KEY = "uid";

    private final Key key;
    private final RedisUtil redisUtil;
    private final UsersRepository userRepository;

    private final long accessExpireMs;
    private final long refreshExpireMs;

    public TokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expire-time}") long accessExpireMs,
            @Value("${jwt.refresh-token-expire-time}") long refreshExpireMs,
            RedisUtil redisUtil,
            UsersRepository userRepository
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisUtil = redisUtil;
        this.userRepository = userRepository;
        this.accessExpireMs = accessExpireMs;
        this.refreshExpireMs = refreshExpireMs;
    }

    public TokenDto generateTokenDto(Authentication authentication, Long userId) {
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String accessToken = generateAccessToken(authentication.getName(), authorities, userId);
        String refreshToken = generateRefreshToken(authentication.getName(), authorities, userId);

        long now = (new Date()).getTime();

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(new Date(now + accessExpireMs).getTime())
                .refreshToken(refreshToken)
                .build();
    }

    public TokenDto reissueAccessToken(String refreshToken) {
        validateToken(refreshToken);

        Claims claims = parseClaims(refreshToken);
        String email = claims.getSubject();

        Object storedRefreshTokenObj = redisUtil.get(email);
        if (storedRefreshTokenObj == null || !storedRefreshTokenObj.toString().equals(refreshToken)) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN, "저장된 토큰과 일치하지 않습니다.");
        }

        if (claims.get("isRefreshToken") == null || !Boolean.TRUE.equals(claims.get("isRefreshToken"))) {
            throw new CustomException(ErrorCode.INVALID_REFRESH_TOKEN, "리프레시 토큰이 아닙니다.");
        }

        String authorities = claims.get(AUTHORITIES_KEY).toString();
        Long userId = claims.get(USER_ID_KEY, Number.class).longValue();

        String newAccessToken = generateAccessToken(email, authorities, userId);
        String newRefreshToken = generateRefreshToken(email, authorities, userId);

        redisUtil.save(email, newRefreshToken, refreshExpireMs);
        log.info("[REDIS] Rotated and saved new refresh token for {}: {}...", email, newRefreshToken.substring(0, 16));

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(newAccessToken)
                .accessTokenExpiresIn(new Date((new Date()).getTime() + accessExpireMs).getTime())
                .refreshToken(newRefreshToken)
                .build();
    }

    private String generateAccessToken(String email, String authorities, Long userId) {
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + accessExpireMs);
        return Jwts.builder()
                .setSubject(email)
                .claim(AUTHORITIES_KEY, authorities)
                .claim(USER_ID_KEY, userId)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String generateRefreshToken(String email, String authorities, Long userId) {
        long now = (new Date()).getTime();
        return Jwts.builder()
                .setSubject(email)
                .claim(AUTHORITIES_KEY, authorities)
                .claim(USER_ID_KEY, userId)
                .setExpiration(new Date(now + refreshExpireMs))
                .claim("isRefreshToken", true)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN, "권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        Long userId = claims.get(USER_ID_KEY, Number.class).longValue();

        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));

        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN, "잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN, "만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN, "지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            throw new CustomException(ErrorCode.INVALID_ACCESS_TOKEN, "JWT 토큰이 잘못되었습니다.");
        }
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }

    public Long getUserId(String token) {
        Claims c = parseClaims(token);
        Number n = c.get(USER_ID_KEY, Number.class);
        return (n == null) ? null : n.longValue();
    }
}

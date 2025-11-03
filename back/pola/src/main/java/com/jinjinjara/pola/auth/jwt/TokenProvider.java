package com.jinjinjara.pola.auth.jwt;

import com.jinjinjara.pola.auth.exception.InvalidTokenException;
import com.jinjinjara.pola.auth.redis.RedisUtil;
import com.jinjinjara.pola.auth.dto.common.TokenDto;
import com.jinjinjara.pola.auth.repository.UserRepository;
import com.jinjinjara.pola.user.entity.Users;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Value;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;
import java.util.Date;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

@Slf4j
@Component
public class TokenProvider {

    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final String USER_ID_KEY = "uid";

    private final Key key;
    private final RedisUtil redisUtil;
    private final UserRepository userRepository; // 추가

    private final long accessExpireMs;
    private final long refreshExpireMs;

    public TokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expire-time}") long accessExpireMs,
            @Value("${jwt.refresh-token-expire-time}") long refreshExpireMs,
            RedisUtil redisUtil,
            UserRepository userRepository // 추가
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisUtil = redisUtil;
        this.userRepository = userRepository; // 추가
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
        Claims claims = parseClaims(refreshToken);

        if (!validateToken(refreshToken) || claims.get("isRefreshToken") == null || !Boolean.TRUE.equals(claims.get("isRefreshToken"))) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = claims.getSubject();
        String authorities = claims.get(AUTHORITIES_KEY).toString();
        Long userId = claims.get(USER_ID_KEY, Number.class).longValue();

        String newAccessToken = generateAccessToken(email, authorities, userId);
        String newRefreshToken = generateRefreshToken(email, authorities, userId);

        redisUtil.save(email, newRefreshToken, refreshExpireMs);
        log.info("[REDIS] saved refresh for {}: {}...", email, newRefreshToken.substring(0,16));

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

    // ✅ 수정된 부분
    public Authentication getAuthentication(String accessToken) {
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        Long userId = claims.get(USER_ID_KEY, Number.class).longValue();

        // Users 엔티티 직접 조회
        Users user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("유효하지 않은 사용자입니다."));

        // Principal을 Users로 설정
        return new UsernamePasswordAuthenticationToken(user, null, authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.", e);
        }
        return false;
    }

    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.", e);
            return e.getClaims();
        }
    }

    public Long getUserId(String token) {
        Claims c = parseClaims(token);
        Number n = c.get(USER_ID_KEY, Number.class);
        return (n == null) ? null : n.longValue();
    }
}

package com.jinjinjara.pola.auth.jwt;

import com.jinjinjara.pola.auth.exception.InvalidTokenException;
import com.jinjinjara.pola.auth.redis.RedisUtil;
import com.jinjinjara.pola.auth.dto.response.TokenDto;
import io.jsonwebtoken.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
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

    private final Key key;
    private final RedisUtil redisUtil;

    private final long accessExpireMs;
    private final long refreshExpireMs;

    public TokenProvider(
            @Value("${jwt.secret}") String secretKey,
            @Value("${jwt.access-token-expire-time}")  long accessExpireMs,
            @Value("${jwt.refresh-token-expire-time}") long refreshExpireMs,
            RedisUtil redisUtil
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.redisUtil = redisUtil;
        this.accessExpireMs = accessExpireMs;
        this.refreshExpireMs = refreshExpireMs;
    }

    public TokenDto generateTokenDto(Authentication authentication) {
        // 권한들 가져오기
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        String accessToken = generateAccessToken(authentication.getName(), authorities);
        String refreshToken = generateRefreshToken(authentication.getName(), authorities);

        long now = (new Date()).getTime();

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .accessTokenExpiresIn(new Date(now + accessExpireMs).getTime())
                .refreshToken(refreshToken)
                .build();
    }

    public TokenDto reissueAccessToken(String refreshToken) {

        // 리프레시 토큰에서 사용자 정보 추출 -> 클레임 확인
        Claims claims = parseClaims(refreshToken);

        // Refresh Token 검증 및 클레임에서 Refresh Token 여부 확인
        if (!validateToken(refreshToken) || claims.get("isRefreshToken") == null || !Boolean.TRUE.equals(claims.get("isRefreshToken"))) {
            throw new InvalidTokenException("유효하지 않은 리프레시 토큰입니다.");
        }

        String email = claims.getSubject();
        String authorities = claims.get(AUTHORITIES_KEY).toString();

        String newAccessToken = generateAccessToken(email, authorities);
        String newRefreshToken = generateRefreshToken(email, authorities);

        redisUtil.save(email, newRefreshToken, refreshExpireMs);
        log.info("[REDIS] saved refresh for {}: {}...", email, newRefreshToken.substring(0,16));

        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(newAccessToken)
                .accessTokenExpiresIn(new Date((new Date()).getTime() + accessExpireMs).getTime())
                .refreshToken(newRefreshToken)
                .build();
    }

    private String generateAccessToken(String email, String authorities) {
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + accessExpireMs);
        return Jwts.builder()
                .setSubject(email)
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    private String generateRefreshToken(String email, String authorities) {
        long now = (new Date()).getTime();
        return Jwts.builder()
                .setSubject(email)
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(new Date(now + refreshExpireMs))
                .claim("isRefreshToken", true) // refreshToken 임을 나타내는 클레임 추가
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    public Authentication getAuthentication(String accessToken) {
        // 토큰 복호화
        Claims claims = parseClaims(accessToken);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 클레임에서 권한 정보 가져오기
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // UserDetails 객체를 만들어서 Authentication 리턴
        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.",e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.",e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.",e);
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.",e);
        }
        return false;
    }

    //JWT 내부의 payload(=claims) 를 꺼내서 정보를 파싱
    private Claims parseClaims(String accessToken) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(accessToken).getBody();
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.",e);
            return e.getClaims();
        }
    }
}
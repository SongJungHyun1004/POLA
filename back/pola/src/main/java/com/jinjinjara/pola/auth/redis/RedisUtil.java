package com.jinjinjara.pola.auth.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

    private static final String REFRESH_TOKEN_PREFIX = "RT:";

    public void save(String key, String value, long ttlMillis) {
        redisTemplate.opsForValue().set(key, value, Duration.ofMillis(ttlMillis));
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public boolean exists(String key) {
        Boolean b = redisTemplate.hasKey(key);
        return b != null && b;
    }

    public void delete(String key) {
        redisTemplate.delete(key);
    }

    /**
     * Refresh Token을 키로 사용하여 사용자 이메일을 저장합니다.
     * 다중 디바이스 로그인을 지원하기 위해 RT:{refreshToken} 형식으로 저장합니다.
     * @param refreshToken Refresh Token 문자열
     * @param email 사용자 이메일
     * @param ttlMillis 만료 시간 (밀리초)
     */
    public void saveRefreshToken(String refreshToken, String email, long ttlMillis) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.opsForValue().set(key, email, Duration.ofMillis(ttlMillis));
    }

    /**
     * Refresh Token으로 사용자 이메일을 조회합니다.
     * @param refreshToken Refresh Token 문자열
     * @return 사용자 이메일 (없으면 null)
     */
    public String getEmailByRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Refresh Token을 Redis에서 삭제합니다. (로그아웃 시 사용)
     * @param refreshToken Refresh Token 문자열
     */
    public void deleteRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        redisTemplate.delete(key);
    }

    /**
     * Refresh Token이 Redis에 존재하는지 확인합니다.
     * @param refreshToken Refresh Token 문자열
     * @return 존재 여부
     */
    public boolean existsRefreshToken(String refreshToken) {
        String key = REFRESH_TOKEN_PREFIX + refreshToken;
        Boolean b = redisTemplate.hasKey(key);
        return b != null && b;
    }
}
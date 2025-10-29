package com.jinjinjara.pola.auth.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, String> redisTemplate;

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
}
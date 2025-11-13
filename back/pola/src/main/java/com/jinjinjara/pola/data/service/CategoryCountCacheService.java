package com.jinjinjara.pola.data.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CategoryCountCacheService {

    private final RedisTemplate<String, String> redisTemplate;

    private String key(Long userId) {
        return "category_count:" + userId;
    }

    public Map<Long, Long> getCategoryCounts(Long userId) {
        Map<Object, Object> raw = redisTemplate.opsForHash().entries(key(userId));

        Map<Long, Long> result = new HashMap<>();
        raw.forEach((k, v) -> {
            result.put(Long.valueOf(k.toString()), Long.valueOf(v.toString()));
        });

        return result;
    }

    public void increment(Long userId, Long categoryId, long delta) {
        redisTemplate.opsForHash()
                .increment(key(userId), categoryId.toString(), delta);
    }

    public void saveAll(Long userId, Map<Long, Long> map) {
        String redisKey = key(userId);
        map.forEach((k, v) ->
                redisTemplate.opsForHash()
                        .put(redisKey, k.toString(), v.toString())
        );
    }

    public void deleteField(Long userId, Long categoryId) {
        redisTemplate.opsForHash()
                .delete(key(userId), categoryId.toString());
    }
}

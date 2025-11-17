package com.jinjinjara.pola.data.repository;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.jinjinjara.pola.data.dto.response.DataResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Duration;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class RemindCacheRepository {

    private final RedisTemplate<String, String> redisTemplate;

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule()) // LocalDateTime 지원
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private String key(Long userId) {
        return "remind:" + userId;
    }

    public List<DataResponse> getRemindFiles(Long userId) {
        String json = redisTemplate.opsForValue().get(key(userId));
        if (json == null) return null;

        try {
            return objectMapper.readValue(json, new TypeReference<List<DataResponse>>() {});
        } catch (Exception e) {
            log.error("[RemindCache] Failed to deserialize remind cache for userId={}", userId, e);
            return null;
        }
    }

    public void deleteRemindFiles(Long userId) {
        try {
            redisTemplate.delete(key(userId));
        } catch (Exception e) {
            log.error("[RemindCache] Failed to delete remind cache for userId={}", userId, e);
        }
    }

    public void saveRemindFiles(Long userId, List<DataResponse> files) {
        try {
            String json = objectMapper.writeValueAsString(files);
            redisTemplate.opsForValue().set(key(userId), json, Duration.ofHours(24));
        } catch (Exception e) {
            log.error("[RemindCache] Failed to save remind cache for userId={}", userId, e);
        }
    }
}

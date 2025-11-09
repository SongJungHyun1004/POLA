package com.jinjinjara.pola.vision.service;

import com.jinjinjara.pola.auth.redis.RedisUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmbeddingCacheService {

    private final RedisUtil redis;

    @Value("${embedding.cache.centroids-ttl-ms:0}")
    private long centroidsTtlMs;

    private static final long DEFAULT_TTL_MS = 86_400_000L;

    // ------- Public API -------

    /** 센트로이드 JSON 로드 */
    public Optional<String> loadCentroidsJson(Long userId) {
        String v = redis.get(keyCentroids(userId));
        return Optional.ofNullable(v).filter(s -> !s.isBlank());
    }

    /** 센트로이드 JSON 저장 (TTL 적용) */
    public void saveCentroidsJson(Long userId, String centroidsJson) {
        long ttl = (centroidsTtlMs > 0) ? centroidsTtlMs : DEFAULT_TTL_MS;
        log.info("[EmbedCache] SAVE uid={} ttlMs={} bytes={}", userId, ttl, centroidsJson.length());
        redis.save(keyCentroids(userId), centroidsJson, ttl);
    }

    /** 메타 JSON 로드 (옵션용) */
    public Optional<String> loadMetaJson(Long userId) {
        String v = redis.get(keyMeta(userId));
        return Optional.ofNullable(v).filter(s -> !s.isBlank());
    }

    /** 메타 JSON 저장 (TTL 적용) */
    public void saveMetaJson(Long userId, String metaJson) {
        long ttl = (centroidsTtlMs > 0) ? centroidsTtlMs : DEFAULT_TTL_MS;
        redis.save(keyMeta(userId), metaJson, ttl);
    }

    /** 해당 유저의 캐시 무효화 */
    public void invalidate(Long userId) {
        redis.delete(keyCentroids(userId));
        redis.delete(keyMeta(userId));
    }

    // ------- Key helpers -------

    private String keyCentroids(Long userId) {
        return "pola:embed:index:" + userId + ":centroids";
    }

    private String keyMeta(Long userId) {
        return "pola:embed:index:" + userId + ":meta";
    }
}

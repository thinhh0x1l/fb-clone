package com.fb.infrastructure.cache;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * Bộ nhớ đệm đa tầng: Caffeine (L1) + Redis (L2)
 * 
 * L1: Caffeine - local cache, cực nhanh (nano giây)
 * L2: Redis - distributed cache, nhanh (milli giây)
 * L3: Database - nguồn dữ liệu gốc
 */
@Slf4j
@Component
public class MultiTierCache {

    private final RedisTemplate<String, Object> redisTemplate;
    
    // L1: Caffeine local cache
    private final Cache<String, Object> localCache;
    
    private static final int LOCAL_CACHE_MAX_SIZE = 10_000;
    private static final int LOCAL_CACHE_TTL_MINUTES = 5;

    public MultiTierCache(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.localCache = Caffeine.newBuilder()
                .maximumSize(LOCAL_CACHE_MAX_SIZE)
                .expireAfterWrite(LOCAL_CACHE_TTL_MINUTES, TimeUnit.MINUTES)
                .recordStats()
                .build();
    }

    /**
     * Lấy dữ liệu từ cache (L1 → L2)
     */
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> type) {
        // Thử L1 trước
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            log.debug("L1 cache hit: {}", key);
            return type.cast(value);
        }

        // Thử L2
        value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            log.debug("L2 cache hit: {}", key);
            localCache.put(key, value); // Warm L1
            return type.cast(value);
        }

        log.debug("Cache miss: {}", key);
        return null;
    }

    /**
     * Lưu dữ liệu vào cả L1 và L2
     */
    public void set(String key, Object value, long ttlSeconds) {
        // Lưu vào L1
        localCache.put(key, value);
        
        // Lưu vào L2
        redisTemplate.opsForValue().set(key, value, ttlSeconds, TimeUnit.SECONDS);
        
        log.debug("Cache set: {} (TTL: {}s)", key, ttlSeconds);
    }

    /**
     * Xóa khỏi cả L1 và L2
     */
    public void delete(String key) {
        localCache.invalidate(key);
        redisTemplate.delete(key);
        log.debug("Cache delete: {}", key);
    }

    /**
     * Xóa nhiều key theo pattern (dùng SCAN thay vì KEYS)
     */
    public long deleteByPattern(String pattern) {
        // Xóa L1 (invalidated by TTL, nhưng có thể invalidate sớm)
        localCache.asMap().keySet().stream()
                .filter(k -> k.startsWith(pattern.replace("*", "")))
                .forEach(localCache::invalidate);

        // Xóa L2 bằng SCAN (non-blocking)
        Set<String> keysToDelete = new java.util.HashSet<>();
        var scanOptions = org.springframework.data.redis.core.ScanOptions.scanOptions()
                .match(pattern)
                .count(100)
                .build();

        try (var cursor = redisTemplate.getConnectionFactory().getConnection().scan(scanOptions)) {
            while (cursor.hasNext()) {
                String key = new String(cursor.next());
                keysToDelete.add(key);
                if (keysToDelete.size() >= 1000) {
                    redisTemplate.delete(keysToDelete);
                    keysToDelete.clear();
                }
            }
        }

        if (!keysToDelete.isEmpty()) {
            redisTemplate.delete(keysToDelete);
        }

        return 0L;
    }

    /**
     * Kiểm tra key có tồn tại không
     */
    public boolean hasKey(String key) {
        if (localCache.getIfPresent(key) != null) {
            return true;
        }
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    /**
     * Tăng counter atomically
     */
    public Long increment(String key) {
        localCache.invalidate(key);
        return redisTemplate.opsForValue().increment(key);
    }

    /**
     * Thêm vào Set
     */
    public Long setAdd(String key, Object... values) {
        localCache.invalidate(key);
        return redisTemplate.opsForSet().add(key, values);
    }

    /**
     * Lấy members của Set
     */
    public Set<Object> setMembers(String key) {
        // Set không cache ở L1 vì thường thay đổi
        return redisTemplate.opsForSet().members(key);
    }

    /**
     * Thêm vào Sorted Set
     */
    public Boolean sortedSetAdd(String key, Object value, double score) {
        localCache.invalidate(key);
        return redisTemplate.opsForZSet().add(key, value, score);
    }

    /**
     * Lấy range từ Sorted Set
     */
    public Set<Object> sortedSetReverseRange(String key, long start, long end) {
        return redisTemplate.opsForZSet().reverseRange(key, start, end);
    }

    /**
     * Lấy thống kê cache
     */
    public String getStats() {
        var stats = localCache.stats();
        return String.format(
                "L1 Cache - Hit rate: %.2f%%, Size: %d, Evictions: %d",
                stats.hitRate() * 100,
                localCache.estimatedSize(),
                stats.evictionCount()
        );
    }
}

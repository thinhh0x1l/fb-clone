package com.fb.batch;

import com.fb.infrastructure.cache.CacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Batch job for cache operations
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CacheBatchJob {

    private final CacheService cacheService;

    /**
     * Batch warmup cache for multiple keys
     */
    @Async
    public CompletableFuture<Void> warmupCache(Set<String> keys) {
        log.info("Warming up cache for {} keys", keys.size());
        
        // TODO: Fetch data and populate cache
        
        log.info("Cache warmup complete");
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Batch invalidate cache for multiple keys
     */
    @Async
    public CompletableFuture<Void> invalidateCache(Set<String> keys) {
        log.info("Invalidating cache for {} keys", keys.size());
        
        for (String key : keys) {
            cacheService.delete(key);
        }
        
        log.info("Cache invalidation complete");
        return CompletableFuture.completedFuture(null);
    }

    /**
     * Batch refresh TTL for multiple keys
     */
    @Async
    public CompletableFuture<Void> refreshTTL(Set<String> keys, long ttlSeconds) {
        log.info("Refreshing TTL for {} keys", keys.size());
        
        for (String key : keys) {
            cacheService.expire(key, ttlSeconds, java.util.concurrent.TimeUnit.SECONDS);
        }
        
        log.info("TTL refresh complete");
        return CompletableFuture.completedFuture(null);
    }
}

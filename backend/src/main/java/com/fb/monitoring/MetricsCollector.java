package com.fb.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Bộ thu thập chỉ số ứng dụng
 */
@Slf4j
@Component
public class MetricsCollector {

    private final MeterRegistry meterRegistry;
    private final AtomicLong activeUsers = new AtomicLong(0);
    private final AtomicLong activeConnections = new AtomicLong(0);

    public MetricsCollector(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        registerGauges();
    }

    private void registerGauges() {
        Gauge.builder("app.users.active", activeUsers, AtomicLong::doubleValue)
                .description("Number of active users")
                .register(meterRegistry);

        Gauge.builder("app.connections.active", activeConnections, AtomicLong::doubleValue)
                .description("Number of active WebSocket connections")
                .register(meterRegistry);
    }

    /**
     * Ghi lại yêu cầu API
     */
    public void recordApiRequest(String endpoint, String method, int status) {
        Counter.builder("app.api.requests")
                .tag("endpoint", endpoint)
                .tag("method", method)
                .tag("status", String.valueOf(status))
                .register(meterRegistry)
                .increment();
    }

    /**
     * Ghi lại thời gian phản hồi API
     */
    public void recordApiResponseTime(String endpoint, long durationMs) {
        Timer.builder("app.api.response.time")
                .tag("endpoint", endpoint)
                .register(meterRegistry)
                .record(durationMs, TimeUnit.MILLISECONDS);
    }

    /**
     * Ghi lại bài viết được tạo
     */
    public void recordPostCreated() {
        Counter.builder("app.posts.created")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Ghi lại tin nhắn được gửi
     */
    public void recordMessageSent() {
        Counter.builder("app.messages.sent")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Ghi lại yêu cầu kết bạn
     */
    public void recordFriendRequest() {
        Counter.builder("app.friends.requests")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Ghi lại cache hit
     */
    public void recordCacheHit() {
        Counter.builder("app.cache.hits")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Ghi lại cache miss
     */
    public void recordCacheMiss() {
        Counter.builder("app.cache.misses")
                .register(meterRegistry)
                .increment();
    }

    /**
     * Cập nhật số lượng người dùng đang hoạt động
     */
    public void setActiveUsers(long count) {
        activeUsers.set(count);
    }

    /**
     * Cập nhật số lượng kết nối đang hoạt động
     */
    public void setActiveConnections(long count) {
        activeConnections.set(count);
    }

    /**
     * Tăng số lượng kết nối đang hoạt động
     */
    public void incrementActiveConnections() {
        activeConnections.incrementAndGet();
    }

    /**
     * Giảm số lượng kết nối đang hoạt động
     */
    public void decrementActiveConnections() {
        activeConnections.decrementAndGet();
    }
}

package com.fb.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Cấu hình async và scheduling
 */
@Configuration
@EnableAsync
@EnableScheduling
public class AsyncConfig {
    // Cấu hình mặc định, có thể tùy chỉnh thread pool
}

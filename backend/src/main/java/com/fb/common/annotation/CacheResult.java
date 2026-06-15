package com.fb.common.annotation;

import java.lang.annotation.*;

/**
 * Annotation caching tự động
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface CacheResult {
    /**
     * Tên cache
     */
    String value();

    /**
     * Key cho cache (hỗ trợ SpEL)
     */
    String key() default "";

    /**
     * Thời gian hết hạn (giây)
     */
    int ttlSeconds() default 300;
}

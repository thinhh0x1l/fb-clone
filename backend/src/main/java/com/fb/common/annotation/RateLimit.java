package com.fb.common.annotation;

import java.lang.annotation.*;

/**
 * Annotation giới hạn tần suất yêu cầu
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RateLimit {
    /**
     * Số lượng yêu cầu tối đa
     */
    int capacity() default 100;

    /**
     * Số token nạp lại
     */
    int refillTokens() default 100;

    /**
     * Thời gian nạp lại (giây)
     */
    int refillDurationSeconds() default 60;
}

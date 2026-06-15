package com.fb.aspect;

import com.fb.common.annotation.RateLimit;
import com.fb.common.exception.TooManyRequestsException;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * Aspect giới hạn tần suất yêu cầu
 */
@Slf4j
@Aspect
@Component
public class RateLimitAspect {

    private final RedisTemplate<String, Object> redisTemplate;

    public RateLimitAspect(RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Around("@annotation(rateLimit)")
    public Object rateLimit(ProceedingJoinPoint joinPoint, RateLimit rateLimit) throws Throwable {
        String key = "ratelimit:" + joinPoint.getSignature().toShortString();
        Object current = redisTemplate.opsForValue().get(key);

        if (current != null) {
            int count = Integer.parseInt(current.toString());
            if (count >= rateLimit.capacity()) {
                throw new TooManyRequestsException("Quá nhiều yêu cầu, vui lòng thử lại sau");
            }
            redisTemplate.opsForValue().increment(key);
        } else {
            redisTemplate.opsForValue().set(key, 1, rateLimit.refillDurationSeconds(), TimeUnit.SECONDS);
        }

        return joinPoint.proceed();
    }
}

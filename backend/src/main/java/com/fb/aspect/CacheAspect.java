package com.fb.aspect;

import com.fb.common.annotation.CacheResult;
import com.fb.infrastructure.cache.MultiTierCache;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

/**
 * Aspect caching tự động
 */
@Slf4j
@Aspect
@Component
public class CacheAspect {

    private final MultiTierCache cache;
    private final ExpressionParser parser = new SpelExpressionParser();

    public CacheAspect(MultiTierCache cache) {
        this.cache = cache;
    }

    @Around("@annotation(cacheResult)")
    public Object cache(ProceedingJoinPoint joinPoint, CacheResult cacheResult) throws Throwable {
        String key = buildCacheKey(cacheResult, joinPoint);

        // Thử lấy từ cache
        Object cached = cache.get(key, Object.class);
        if (cached != null) {
            log.debug("Cache hit: {}", key);
            return cached;
        }

        // Thực thi method
        Object result = joinPoint.proceed();

        // Lưu vào cache
        if (result != null) {
            cache.set(key, result, cacheResult.ttlSeconds());
            log.debug("Cache set: {} (TTL: {}s)", key, cacheResult.ttlSeconds());
        }

        return result;
    }

    private String buildCacheKey(CacheResult cacheResult, ProceedingJoinPoint joinPoint) {
        StringBuilder keyBuilder = new StringBuilder(cacheResult.value()).append(":");

        if (cacheResult.key().isEmpty()) {
            keyBuilder.append(joinPoint.getSignature().toShortString());
        } else {
            StandardEvaluationContext context = new StandardEvaluationContext();
            String[] paramNames = ((org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature()).getParameterNames();
            Object[] args = joinPoint.getArgs();

            if (paramNames != null) {
                for (int i = 0; i < paramNames.length; i++) {
                    context.setVariable(paramNames[i], args[i]);
                }
            }

            try {
                keyBuilder.append(parser.parseExpression(cacheResult.key()).getValue(context, String.class));
            } catch (Exception e) {
                keyBuilder.append("default");
            }
        }

        return keyBuilder.toString();
    }
}

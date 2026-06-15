package com.fb.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * Aspect ghi log thời gian thực thi
 */
@Slf4j
@Aspect
@Component
public class ExecutionTimeAspect {

    @Around("@annotation(com.fb.common.annotation.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        log.debug("Bắt đầu thực thi: {}", methodName);

        try {
            Object result = joinPoint.proceed();
            long executionTime = System.currentTimeMillis() - startTime;
            log.info("Hoàn thành {} trong {}ms", methodName, executionTime);
            return result;
        } catch (Throwable ex) {
            long executionTime = System.currentTimeMillis() - startTime;
            log.error("Lỗi {} sau {}ms: {}", methodName, executionTime, ex.getMessage());
            throw ex;
        }
    }
}

package com.fb.aspect;

import com.fb.common.annotation.AuditLog;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Aspect ghi log kiểm toán
 */
@Slf4j
@Aspect
@Component
public class AuditLogAspect {

    @Around("@annotation(auditLog)")
    public Object audit(ProceedingJoinPoint joinPoint, AuditLog auditLog) throws Throwable {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userId = auth != null ? auth.getName() : "anonymous";

        log.info("AUDIT: {} | User: {} | Method: {} | Time: {}",
                auditLog.action(),
                userId,
                joinPoint.getSignature().toShortString(),
                LocalDateTime.now());

        return joinPoint.proceed();
    }
}

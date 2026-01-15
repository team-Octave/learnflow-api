package com.teamexp.learnflowapi.global.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
@Slf4j
public class LogTraceAspect {
    @Around("@annotation(com.teamexp.learnflowapi.global.common.annotation.LogTrace)")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            log.info("[START] Method: {}, Args: {}", methodName, joinPoint.getArgs());
            Object result = joinPoint.proceed();
            long endTime = System.currentTimeMillis();
            log.info("[END] Method: {}, Time: {}ms", methodName, (endTime - startTime));
            return result;
        } catch (Exception e) {
            log.error("[ERROR] Method: {}, Message: {}", methodName, e.getMessage());
            throw e;
        }
    }
}

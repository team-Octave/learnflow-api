package com.teamexp.learnflowapi.global.common.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * @LogTrace 어노테이션이 붙은 메서드의 시작, 종료, 실행 시간 및 예외를 로깅하는 Aspect 클래스입니다.
 */
@Aspect
@Component
@Slf4j
public class LogTraceAspect {

    /**
     * 대상 메서드 실행 전후에 로그를 남깁니다.
     * 코드래빗 피드백 반영: 민감 정보 노출 방지를 위해 인자(Args) 로깅을 제외하고, Throwable로 모든 예외를 캡처합니다.
     *
     * @param joinPoint 실행 지점 정보
     * @return 메서드 실행 결과
     * @throws Throwable 메서드 실행 중 발생한 예외
     */
    @Around("@annotation(com.teamexp.learnflowapi.global.common.annotation.LogTrace)")
    public Object execute(ProceedingJoinPoint joinPoint) throws Throwable {
        long startTime = System.currentTimeMillis();
        String methodName = joinPoint.getSignature().toShortString();

        try {
            log.info("[START] Method: {}", methodName);

            Object result = joinPoint.proceed();

            long endTime = System.currentTimeMillis();
            log.info("[END] Method: {}, Time: {}ms", methodName, (endTime - startTime));
            return result;
        } catch (Throwable t) {
            // 모든 예외 및 에러 상황에 대해 로그 기록
            log.error("[ERROR] Method: {}, Message: {}", methodName, t.getMessage());
            throw t;
        }
    }
}

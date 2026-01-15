package com.teamexp.learnflowapi.global.common.annotation;

import java.lang.annotation.*;

/**
 * 메서드 실행 궤적(Trace)을 로깅하기 위한 마커 어노테이션입니다.
 * 이 어노테이션이 부착된 메서드는 실행 시간과 시작/종료 로그가 자동으로 기록됩니다.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface LogTrace {
}

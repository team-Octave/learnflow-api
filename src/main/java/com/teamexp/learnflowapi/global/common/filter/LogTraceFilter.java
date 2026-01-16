package com.teamexp.learnflowapi.global.common.filter;

import jakarta.servlet.*;
import org.slf4j.MDC;
import java.io.IOException;
import java.util.UUID;

/**
 * 모든 HTTP 요청에 대해 고유한 traceId를 생성하여 로그에 부여하는 필터입니다.
 */
public class LogTraceFilter implements Filter {

    /**
     * 요청 시작 시 traceId를 생성하여 MDC에 저장하고, 요청 종료 시 특정 키만 제거합니다.
     * 코드래빗 피드백 반영: MDC.clear() 대신 remove()를 사용하여 다른 MDC 데이터를 보존합니다.
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {

        // 고유 식별자 발급 (8자리)
        String traceId = UUID.randomUUID().toString().substring(0, 8);
        MDC.put("traceId", traceId);

        try {
            chain.doFilter(request, response);
        } finally {
            // 코드래빗 피드백: 다른 MDC 데이터에 영향을 주지 않도록 traceId만 정밀 타격하여 삭제
            MDC.remove("traceId");
        }
    }
}

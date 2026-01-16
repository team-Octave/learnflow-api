package com.teamexp.learnflowapi.global.common.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;

@Slf4j
@Component
public class RequestResponseLoggingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        // 1. 캐싱 래퍼로 감싸기 (JSON Body 재사용 가능)
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            // 2. 비즈니스 로직 종료 후 로그 남기기
            logRequest(requestWrapper);
            logResponse(responseWrapper);

            // 3. 클라이언트에게 응답 본문 복사 (중요!)
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(ContentCachingRequestWrapper request) {
        String traceId = MDC.get("traceId");
        String body = new String(request.getContentAsByteArray());
        if (!body.isEmpty()) {
            log.info("[Request Body] traceId={}, payload={}", traceId, body);
        }
    }

    private void logResponse(ContentCachingResponseWrapper response) {
        String traceId = MDC.get("traceId");
        String body = new String(response.getContentAsByteArray());
        log.info("[Response Body] traceId={}, payload={}", traceId, body);
    }
}

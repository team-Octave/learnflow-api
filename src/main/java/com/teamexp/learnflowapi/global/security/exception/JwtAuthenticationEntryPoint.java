package com.teamexp.learnflowapi.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.MDC; // 1. MDC 임포트 추가
import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {

        String exception = (String) request.getAttribute("jwt_exception");

        String code = "UNAUTHORIZED";
        String message = "인증이 필요합니다.";

        if ("TOKEN_EXPIRED".equals(exception)) {
            code = "TOKEN_EXPIRED";
            message = "토큰이 만료되었습니다.";
        } else if ("TOKEN_INVALID".equals(exception)) {
            code = "TOKEN_INVALID";
            message = "유효하지 않은 토큰입니다.";
        } else if ("UNKNOWN_ERROR".equals(exception)) {
            code = "UNKNOWN_ERROR";
            message = "알 수 없는 오류로 인해 인증에 실패했습니다.";
        }

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json; charset=UTF-8");

        // 2. MDC에서 traceId를 꺼내서 응답 바디에 넣어줍니다.
        // LogTraceFilter가 Security 필터보다 앞에 있으므로 여기서도 ID를 꺼낼 수 있습니다.
        BaseResponse<?> body = BaseResponse.error(code, message, MDC.get("traceId"));

        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}

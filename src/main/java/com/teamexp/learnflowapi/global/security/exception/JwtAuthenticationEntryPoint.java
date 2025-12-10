package com.teamexp.learnflowapi.global.security.exception;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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

        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType("application/json; charset=UTF-8");

        BaseResponse<?> body = BaseResponse.error("TOKEN_EXPIRED", "토큰이 만료되었습니다.");
        response.getWriter().write(new ObjectMapper().writeValueAsString(body));
    }
}

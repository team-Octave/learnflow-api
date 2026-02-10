package com.teamexp.learnflowapi.global.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
public class InternalApiKeyFilter extends OncePerRequestFilter {

    private final String expectedKey;
    private static final String INTERNAL_API_PREFIX = "/api/internal/";
    private static final String HEADER_API_KEY = "X-Internal-Api-Key";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String requestUri = request.getRequestURI();

        // 내부 API 요청인 경우에만 검증 수행
        if (requestUri.startsWith(INTERNAL_API_PREFIX)) {
            String requestKey = request.getHeader(HEADER_API_KEY);

            if (requestKey != null && MessageDigest.isEqual(
                expectedKey.getBytes(StandardCharsets.UTF_8),
                requestKey.getBytes(StandardCharsets.UTF_8))) {
                // 인증 성공
                SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken("system", null, List.of(new SimpleGrantedAuthority("ROLE_SYSTEM")))
                );
                log.info("✅ [Internal API] Authorized access to {} from {}", requestUri, request.getRemoteAddr());
            } else {
                // 인증 실패
                log.warn("🚨 [Internal API] Unauthorized access attempt to {} from {} (Key: {})",
                    requestUri, request.getRemoteAddr(), (requestKey == null ? "null" : "REDACTED"));

                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("text/plain;charset=UTF-8"); // Content-Type 헤더 추가
                response.getWriter().write("Unauthorized: Invalid API Key");
                return; // 필터 체인 중단
            }
        }

        filterChain.doFilter(request, response);
    }
}

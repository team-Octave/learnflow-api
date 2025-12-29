package com.teamexp.learnflowapi.global.utils;

import com.teamexp.learnflowapi.global.properties.CookieProperties;
import com.teamexp.learnflowapi.global.properties.CookieProperties.Refresh;
import java.time.Duration;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    private final Refresh refresh;

    public CookieUtil(CookieProperties cookieProperties) {
        this.refresh = cookieProperties.getRefresh();
    }

    /**
     * Refresh Token 쿠키 생성
     * @param token         리프레시 토큰
     * @return              ResponseCookie 객체
    * */
    public ResponseCookie createRefreshTokenCookie(String token) {

        return ResponseCookie.from(refresh.getName(), token)
            .httpOnly(refresh.isHttpOnly())
            .secure(refresh.isSecure())
            .sameSite(refresh.getSameSite())
            .path(refresh.getPath())
            .maxAge(Duration.ofSeconds(refresh.getMaxAge()))
            .build();
    }

    public ResponseCookie deleteCookie(String name) {
        return ResponseCookie.from(name, "")
            .path("/")
            .maxAge(0)
            .build();
    }
}

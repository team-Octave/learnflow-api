package com.teamexp.learnflowapi.global.security.jwt;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.assertj.core.api.Assertions.assertThat;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    private static final String USER_ID = "test-uuid-id";
    private static final String EMAIL = "test@test.com";
    private static final String ROLE = "MEMBER";
    private static final String NICKNAME = "tester";

    @BeforeEach
    void setup() {
        jwtTokenProvider = new JwtTokenProvider();

        String rawSecretKey = "test-secret-key-for-jjwt-hs256-32bytes!";
        String base64Secret  = Base64.getEncoder().encodeToString(rawSecretKey.getBytes());

        ReflectionTestUtils.setField(jwtTokenProvider, "secretKey", base64Secret);

        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInMs", 60_000); // 60s
        ReflectionTestUtils.setField(jwtTokenProvider, "refreshTokenValidityInMs", 60_000L); // 60s
    }

    @Test
    @DisplayName("AccessToken 생성 - 클레임 포함")
    void tc9_createAccessToken_with_Claims() {
        String accessToken = jwtTokenProvider.createAccessToken(USER_ID, EMAIL, ROLE, NICKNAME);

        assertThat(accessToken).isNotNull().isNotEmpty();

        Claims claims = jwtTokenProvider.parseToken(accessToken);

        assert claims.getSubject().equals(USER_ID);
        assert claims.get("email", String.class).equals(EMAIL);
        assert claims.get("role", String.class).equals(ROLE);
        assert claims.get("nickname", String.class).equals(NICKNAME);
    }

    @Test
    @DisplayName("토큰 검증 성공")
    void tc10_validateToken_success() {
        String accessToken = jwtTokenProvider.createAccessToken(USER_ID, EMAIL, ROLE, NICKNAME);

        boolean result = jwtTokenProvider.validateToken(accessToken);

        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("토큰 검증 실패 - 만료")
    void tc11_validate_token_fail() throws InterruptedException {
        // givne: 만료 토큰
        ReflectionTestUtils.setField(jwtTokenProvider, "accessTokenValidityInMs", 0L);; // 즉시 만료

        String accessToken = jwtTokenProvider.createAccessToken(USER_ID, EMAIL, ROLE, NICKNAME);

        // when : 10ms 대기 → 토큰이 만료된 상태로 검증 시도
        Thread.sleep(10);
        boolean result = jwtTokenProvider.validateToken(accessToken);

        // then : 검증 실패
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("토큰 검증 실패 - 변조")
    void tc12_validate_token_fail_tampered() {
        String accessToken = jwtTokenProvider.createAccessToken(USER_ID, EMAIL, ROLE, NICKNAME);
        // 변조된 토큰 생성
        String tamperedToken = accessToken + "tampered";

        boolean result = jwtTokenProvider.validateToken(tamperedToken);

        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("토큰 파싱 - 유저 ID 추출")
    void tc13_parse_userid_from_subject() {
        String accessToken = jwtTokenProvider.createAccessToken(USER_ID, EMAIL, ROLE, NICKNAME);
        // 변조된 토큰 생성

        Claims claims = jwtTokenProvider.parseToken(accessToken);

        assert claims.getSubject().equals(USER_ID);
    }

    @Test
    @DisplayName("토큰 파싱 - role 추출")
    void tc14_parse_role_from_claims() {
        String accessToken = jwtTokenProvider.createAccessToken(USER_ID, EMAIL, ROLE, NICKNAME);

        Claims claims = jwtTokenProvider.parseToken(accessToken);

        assert claims.get("role", String.class).equals(ROLE);
    }
}

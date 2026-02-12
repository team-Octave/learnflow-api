package com.teamexp.learnflowapi.integration.user;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.global.security.jwt.JwtTokenProvider;
import com.teamexp.learnflowapi.integration.config.TestMockConfig;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.model.vo.UserRole;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestMockConfig.class)
public class UserIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    private User user;
    private String accessToken;

    @BeforeEach
    public void setUp() {
        user = userRepository.save(User.createUser("test@test.com", "password", "tester", UserRole.MEMBER));

        accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(), user.getEmail(), user.getRole().name(), user.getNickname()
        );
    }

    @Test
    @DisplayName("미로그인_API_접근_차단")
    void tc1_not_logged_in_access_blocked() throws Exception {
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Disabled("TODO : JwtAuthenticationFilter에서 예외로 throwing 되는 문제. Filter단에서 status 처리 필요")
    @Test
    @DisplayName("토큰에 임의값 추가 접근 차단")
    void tc3_Token_Tampering_access_blocked() throws Exception {
        // 토큰 변조 (임의의 문자열 추가)
        String tamperedToken = accessToken + "test";

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tamperedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("정상 토큰 접근 허용")
    void tc4_valid_token_access_allowed() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.email").value(user.getEmail()))
                .andExpect(jsonPath("$.data.nickname").value(user.getNickname()))
                .andExpect(jsonPath("$.data.role").value(user.getRole().name()));
    }

    @Test
    @DisplayName("회원 탈퇴 성공 + 소프트 삭제 적용")
    void tc23_withdraw_user_success_soft_delete_applied() throws Exception {
        // 회원 탈퇴 요청
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // Then: DB에서 del_flag=true 확인 (soft delete 검증 핵심)
        var deletedUser = userRepository.findByEmail(user.getEmail()).orElseThrow();
        assertThat(deletedUser.getDelFlag()).isTrue();
    }

    @Test
    @DisplayName("중복 탈퇴 요청 실패 - 이미 탈퇴된 유저")
    void tc24_withdraw_user_fail_already_withdrawn() throws Exception {
        // Given: 1차 탈퇴 성공
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // When & Then: 2차 탈퇴 요청
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("탈퇴 유저 로그인 실패")
    void tc25_withdrawn_user_login_fail() throws Exception {
        // Given: 유저 탈퇴
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // When & Then: 탈퇴 유저로 로그인 시도
        String loginBody = objectMapper.writeValueAsString(
                new java.util.HashMap<String, Object>() {{
                    put("email", user.getEmail());
                    put("password", "password");
                }}
        );

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginBody))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("탈퇴 유저 /me 조회 실패")
    void tc26_withdrawn_user_me_lookup_fail() throws Exception {
        // Given: 유저 탈퇴
        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk());

        // When & Then: 탈퇴 유저로 /me 조회 시도
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isConflict());
    }
}

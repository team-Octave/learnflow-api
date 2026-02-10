package com.teamexp.learnflowapi.integration.user;

import com.teamexp.learnflowapi.global.security.jwt.JwtTokenProvider;
import com.teamexp.learnflowapi.integration.config.TestMockConfig;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.model.vo.UserRole;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.jwt.access-expiration=1"
})
@AutoConfigureMockMvc
@Transactional
@Import(TestMockConfig.class)
class UserTokenExpirationIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    JwtTokenProvider jwtTokenProvider;
    @Autowired
    UserRepository userRepository;

    User user;

    @BeforeEach
    void setUp() {
        user = userRepository.save(
                User.createUser("test@test.com", "password", "tester", UserRole.MEMBER)
        );
    }

    @Disabled("TODO : JwtAuthenticationFilter에서 예외로 throwing 되는 문제. Filter단에서 status 처리 필요")
    @Test
    @DisplayName("만료된 토큰 접근 차단")
    void tc2_expired_token_access_blocked() throws Exception {
        String token = jwtTokenProvider.createAccessToken(
                user.getUserId(), user.getEmail(), user.getRole().name(), user.getNickname()
        );

        Thread.sleep(5);

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }
}

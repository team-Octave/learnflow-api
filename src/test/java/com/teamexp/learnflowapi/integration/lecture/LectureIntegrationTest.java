package com.teamexp.learnflowapi.integration.lecture;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.global.security.jwt.JwtTokenProvider;
import com.teamexp.learnflowapi.integration.config.TestMockConfig;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.model.vo.UserRole;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestMockConfig.class)
public class LectureIntegrationTest {

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
    void setUp() {
        user = userRepository.save(
                User.createUser("test@test.com", "password", "tester", UserRole.MEMBER)
        );

        accessToken = jwtTokenProvider.createAccessToken(
                user.getUserId(),
                user.getEmail(),
                user.getRole().name(),
                user.getNickname()
        );
    }

    @Test
    @DisplayName("강의 생성 성공")
    void tc5_lecture_create_success() throws Exception {
        String body= """
                 {
                 "title": "테스트용 강의",
                 "description": "테스트용",
                 "categoryId": 1,
                 "level": "BEGINNER",
                 "thumbnailUrl": "https://example.com/thumb.png"
                }
                """;

        mockMvc.perform(post("/api/v2/lectures")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.title").value("테스트용 강의"))
                .andExpect(jsonPath("$.data.description").value("테스트용"))
                .andExpect(jsonPath("$.data.categoryId").value(1))
                .andExpect(jsonPath("$.data.level").value("BEGINNER"))
                .andExpect(jsonPath("$.data.thumbnailUrl").value("https://example.com/thumb.png"));
    }

    @Test
    @DisplayName("강의 생성 실패(미로그인)")
    void tc6_lecture_create_fail_not_logged_in() throws Exception {
        String body= """
                 {
                 "title": "테스트용 강의",
                 "description": "테스트용",
                 "categoryId": 1,
                 "level": "BEGINNER",
                 "thumbnailUrl": "https://example.com/thumb.png"
                }
                """;

        mockMvc.perform(post("/api/v2/lectures")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @DisplayName("강의 생성 실패(제목 미입력)")
    void tc7_lecture_create_fail() throws Exception {
        String body= """
                 {
                 "title": "",
                 "description": "테스트용",
                 "categoryId": 1,
                 "level": "BEGINNER",
                 "thumbnailUrl": "https://example.com/thumb.png"
                }
                """;

        mockMvc.perform(post("/api/v2/lectures")
                        .header("Authorization", "Bearer " + accessToken)
                        .contentType("application/json")
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_INPUT_VALUE"))
                .andExpect(jsonPath("$.message").value("잘못된 입력 값입니다."));

    }
}

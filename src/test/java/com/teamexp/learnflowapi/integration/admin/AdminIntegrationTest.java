package com.teamexp.learnflowapi.integration.admin;

import com.teamexp.learnflowapi.admin.model.Approval;
import com.teamexp.learnflowapi.admin.repository.ApprovalRepository;
import com.teamexp.learnflowapi.global.security.jwt.JwtTokenProvider;
import com.teamexp.learnflowapi.integration.config.TestMockConfig;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureLevel;
import com.teamexp.learnflowapi.lecture.model.LectureStatistic;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import com.teamexp.learnflowapi.lecture.model.PaymentType;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
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
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;


import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@Import(TestMockConfig.class)
public class AdminIntegrationTest {
    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    LectureRepository lectureRepository;

    @Autowired
    LectureStatisticRepository lectureStatisticRepository;

    @Autowired
    ApprovalRepository approvalRepository;

    private User admin;
    private User instructor;
    private User member;

    private String adminToken;
    private String memberToken;

    private Lecture lecture; // 승인/반려 대상

    @BeforeEach
    void setUp() {
        admin = userRepository.save(
                User.createUser("admin@test.com", "password", "admin", UserRole.ADMIN)
        );

        instructor = userRepository.save(
                User.createUser("instructor@test.com", "password", "instructor", UserRole.MEMBER)
        );

        member = userRepository.save(
                User.createUser("member@test.com", "password", "member", UserRole.MEMBER)
        );

        adminToken = jwtTokenProvider.createAccessToken(
                admin.getUserId(), admin.getEmail(), admin.getRole().name(), admin.getNickname()
        );

        memberToken = jwtTokenProvider.createAccessToken(
                member.getUserId(), member.getEmail(), member.getRole().name(), member.getNickname()
        );

        lecture = Lecture.createLecture(
                "approval-target-lecture",
                "desc",
                LectureLevel.BEGINNER,
                1,
                instructor.getUserId(),
                "http://example.com/thumb.jpg",
                PaymentType.FREE
        );

        ReflectionTestUtils.setField(lecture, "status", LectureStatus.SUBMITTED);

        lecture = lectureRepository.saveAndFlush(lecture);

        LectureStatistic stat = LectureStatistic.createInitial(lecture);
        lectureStatisticRepository.saveAndFlush(stat);

        approvalRepository.saveAndFlush(Approval.create(lecture.getId()));
    }

    @Test
    @DisplayName("어드민 승인 성공")
    void tc17_admin_approve_success() throws Exception {
        String body = """
                {
                  "status": "APPROVED"
                }
                """;

        mockMvc.perform(
                        patch("/api/v1/admin/approvals/{lectureId}", lecture.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("어드민 반려 성공")
    void tc18_admin_reject_success() throws Exception {
        String body = """
                {
                  "status": "REJECTED",
                  "reason": "테스트 반려"
                }
                """;

        mockMvc.perform(
                        patch("/api/v1/admin/approvals/{lectureId}", lecture.getId())
                                .header("Authorization", "Bearer " + adminToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("어드민 권한 없는 계정으로 승인 실패")
    void tc19_not_admin_approve_fail() throws Exception {
        String body = """
                {
                  "status": "APPROVED"
                }
                """;

        mockMvc.perform(
                        patch("/api/v1/admin/approvals/{lectureId}", lecture.getId())
                                .header("Authorization", "Bearer " + memberToken)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(body)
                )
                .andExpect(status().isForbidden());
    }
}

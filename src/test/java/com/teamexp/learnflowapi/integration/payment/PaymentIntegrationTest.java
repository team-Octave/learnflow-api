package com.teamexp.learnflowapi.integration.payment;

import com.teamexp.learnflowapi.global.security.jwt.JwtTokenProvider;
import com.teamexp.learnflowapi.integration.config.TestMockConfig;
import com.teamexp.learnflowapi.payment.model.PaymentHistory;
import com.teamexp.learnflowapi.payment.model.constant.PlanType;
import com.teamexp.learnflowapi.payment.repository.PaymentHistoryRepository;
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
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@Import(TestMockConfig.class)
public class PaymentIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    JwtTokenProvider jwtTokenProvider;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PaymentHistoryRepository paymentHistoryRepository;

    private User userA;
    private User userB;
    private String accessTokenA;

    @BeforeEach
    void setUp() {
        userA = userRepository.save(
                User.createUser("userA@test.com", "pw", "userA", UserRole.MEMBER)
        );
        userB = userRepository.save(
                User.createUser("userB@test.com", "pw", "userB", UserRole.MEMBER)
        );
        accessTokenA = jwtTokenProvider.createAccessToken(
                userA.getUserId(),
                userA.getEmail(),
                userA.getRole().name(),
                userA.getNickname()
        );
    }

    @Test
    @DisplayName("결제 컨펌 실패 - 미로그인")
    void tc27_payment_confirm_fail_not_login() throws Exception {
        String body = """
                { "paymentKey": "payKey-123", "orderId": "order-123", "amount": 10000 }
                """;

        mockMvc.perform(post("/api/v1/payments/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("결제 컨펌 실패 - 필수값 누락(Validation)")
    void tc28_payment_confirm_fail_validation_error() throws Exception {
        String body = """
                { "paymentKey": "", "orderId": "order-123", "amount": 9900 }
                """;

        mockMvc.perform(post("/api/v1/payments/confirm")
                        .header("Authorization", "Bearer " + accessTokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("결제내역 조회 실패- 미로그인")
    void tc29_payment_history_fail_not_login() throws Exception {
        mockMvc.perform(get("/api/v1/payments/history"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("결제내역 조회 성공 - 본인 결제내역만 조회(정보 노출 방지)")
    void tc30_payment_history_only_mine_success() throws Exception {
        // Given: userA가 결제내역 2건, userB가 결제내역 1건 보유
        paymentHistoryRepository.save(PaymentHistory.create(
                userA.getUserId(), "orderA-1", "payKeyA-1", 9900L, PlanType.ONE_MONTH, Instant.now()
        ));
        paymentHistoryRepository.save(PaymentHistory.create(
                userA.getUserId(), "orderA-2", "payKeyA-2", 29900L, PlanType.THREE_MONTHS, Instant.now()
        ));
        paymentHistoryRepository.save(PaymentHistory.create(
                userB.getUserId(), "orderB-1", "payKeyB-1", 199000L, PlanType.YEAR, Instant.now()
        ));

        // When
        var result = mockMvc.perform(get("/api/v1/payments/history")
                        .header("Authorization", "Bearer " + accessTokenA))
                .andExpect(status().isOk())
                .andReturn();

        //then: userA의 결제내역 2건만 조회되고 userB의 결제내역은 노출되지 않음
        String json = result.getResponse().getContentAsString();

        assertThat(json).contains("1개월");
        assertThat(json).contains("3개월");


        assertThat(json).doesNotContain("12개월");
    }
}

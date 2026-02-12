package com.teamexp.learnflowapi.payment.service;

import com.teamexp.learnflowapi.membership.service.dto.PaymentCompletedEvent;
import com.teamexp.learnflowapi.payment.dto.request.PaymentConfirmRequest;
import com.teamexp.learnflowapi.payment.exception.PaymentAlreadyProcessedException;
import com.teamexp.learnflowapi.payment.exception.PaymentAmountMismatchException;
import com.teamexp.learnflowapi.payment.exception.TossErrorException;
import com.teamexp.learnflowapi.payment.model.PaymentHistory;
import com.teamexp.learnflowapi.payment.model.constant.PaymentStatus;
import com.teamexp.learnflowapi.payment.model.constant.PlanType;
import com.teamexp.learnflowapi.payment.repository.PaymentHistoryRepository;
import com.teamexp.learnflowapi.payment.service.dto.PaymentDto;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestClient;

import java.time.OffsetDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    PaymentService paymentService;

    @Mock
    RestClient tossRestClient;

    @Mock
    TossProps tossProps;

    @Mock
    PaymentHistoryRepository paymentHistoryRepository;

    @Mock
    ApplicationEventPublisher eventPublisher;


    // RestClient 체이닝용 Mock들 (fluent API라 중간 단계 인터페이스를 각각 잡아야 함)
    @Mock
    RestClient.RequestBodyUriSpec requestBodyUriSpec;
    @Mock
    RestClient.RequestBodySpec requestBodySpec;
    @Mock
    RestClient.ResponseSpec responseSpec;

    // 헬퍼 메서드들
    private PaymentConfirmRequest confirmReq(String payKey, String orderId, long amount) {
        return new PaymentConfirmRequest(payKey, orderId, amount);
    }

    private PaymentDto paymentDto(String paymentKey, String orderId, String orderName, long totalAmount) {
        return new PaymentDto(
                paymentKey,
                "NORMAL",
                orderId,
                orderName,
                "CARD",
                totalAmount,
                PaymentStatus.DONE,
                OffsetDateTime.now().toString(),
                null
        );
    }

    /**
     * Toss confirm API 체이닝을 한 번에 세팅.
     * - toThrow != null 이면: onStatus에서 예외를 던지게 해서 TossErrorException 흐름을 테스트에서 강제로 만든다.
     * - toThrow == null 이면: 정상 응답 PaymentDto를 body(PaymentDto.class)에서 반환한다.
     */
    private void mockTossConfirmApi(PaymentDto dto, RuntimeException toThrow) {
        when(tossProps.getSecretKey()).thenReturn("test_secret_key");
        when(tossProps.getConfirmUrl()).thenReturn("https://example.com/confirm");

        when(tossRestClient.post()).thenReturn(requestBodyUriSpec);
        when(requestBodyUriSpec.uri(anyString())).thenReturn(requestBodySpec);

        when(requestBodySpec.header(anyString(), anyString())).thenReturn(requestBodySpec);
        when(requestBodySpec.contentType(any(MediaType.class))).thenReturn(requestBodySpec);

        when(requestBodySpec.body(any(PaymentConfirmRequest.class))).thenReturn(requestBodySpec);

        when(requestBodySpec.retrieve()).thenReturn(responseSpec);

        when(responseSpec.onStatus(any(), any())).thenReturn(responseSpec);

        if (toThrow != null) {
            when(responseSpec.body(eq(PaymentDto.class))).thenThrow(toThrow);
        } else {
            when(responseSpec.body(eq(PaymentDto.class))).thenReturn(dto);
        }
    }

    @Test
    @DisplayName("결제 컨펌 실패 - 이미 처리된 orderId(중복 결제 방지)")
    void tc37_payment_confirm_fail_already_processed_orderId() {
        String userId = "user-123";
        PaymentConfirmRequest request = confirmReq("payKey-123", "order-123", 9900L);

        when(paymentHistoryRepository.existsByOrderId(request.orderId())).thenReturn(true);

        assertThatThrownBy(() -> paymentService.tossConfirm(request, userId))
                .isInstanceOf(PaymentAlreadyProcessedException.class);

        verify(tossRestClient, never()).post();
        verify(paymentHistoryRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("결제 컨펌 실패 - Toss confirm API 호출 중 오류")
    void tc38_payment_confirm_fail_toss_api_error() {
        String userId = "user-123";
        PaymentConfirmRequest request = confirmReq("payKey-123", "order-234", 9900L);

        when(paymentHistoryRepository.existsByOrderId(request.orderId())).thenReturn(false);

        mockTossConfirmApi(null, new TossErrorException());

        assertThatThrownBy(() -> paymentService.tossConfirm(request, userId))
                .isInstanceOf(TossErrorException.class);

        verify(paymentHistoryRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("결제 컨펌 실패 - 결제 금액 불일치")
    void tc39_payment_confirm_fail_amount_mismatch() {
        String userId = "user-123";
        PaymentConfirmRequest request = confirmReq("payKey-123", "order-345", 9900L);

        when(paymentHistoryRepository.existsByOrderId(request.orderId())).thenReturn(false);

        PaymentDto tossResponse = paymentDto("payKey-123", "order-345", "1개월 이용권", 19000L);
        mockTossConfirmApi(tossResponse, null);

        assertThatThrownBy(() -> paymentService.tossConfirm(request, userId))
                .isInstanceOf(PaymentAmountMismatchException.class);

        verify(paymentHistoryRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("결제 컨펌 성공 - 결제이력 저장 + 결제완료 이벤트 발행")
    void tc40_payment_confirm_success() {
        String userId = "user-123";
        PaymentConfirmRequest request = confirmReq("payKey-123", "order-456", 9900L);

        when(paymentHistoryRepository.existsByOrderId(request.orderId())).thenReturn(false);

        PaymentDto tossResponse = paymentDto("payKey-123", "order-456", "1개월 이용권", 9900L);
        mockTossConfirmApi(tossResponse, null);

        paymentService.tossConfirm(request, userId);

        assertThat(tossResponse).isNotNull();

        verify(paymentHistoryRepository, times(1)).save(any(PaymentHistory.class));
        verify(eventPublisher, times(1)).publishEvent(any(PaymentCompletedEvent.class));
    }


    // parameterizedTest 케이스마다 독립 실행됨
    @ParameterizedTest(name = "[{index}] orderName={0} -> expected={1}")
    @CsvSource({
            "'1개월 이용권 - 1개월', ONE_MONTH",
            "'3개월 이용권 - 3개월', THREE_MONTHS",
            "'6개월 이용권 - 6개월', HALF_YEAR",
            "'12개월 이용권 - 12개월', YEAR",
    })
    @DisplayName("결제 컨펌 성공 - 플랜 타입 매핑")
    void tc41_payment_confirm_success_plan_type_mapping(String orderName, PlanType expectedPlanType) {
        String userId = "user-123";
        // otherId를 케이스별 다르게 주기위해 설정(중복/꼬임 방지)
        String orderId = "order-"+expectedPlanType.name();

        PaymentConfirmRequest request = confirmReq("payKey-123", orderId, 9900L);

        when(paymentHistoryRepository.existsByOrderId(request.orderId())).thenReturn(false);

        PaymentDto tossResponse = paymentDto("payKey-123", orderId, orderName, 9900L);
        mockTossConfirmApi(tossResponse, null);

        paymentService.tossConfirm(request, userId);

        ArgumentCaptor<PaymentHistory> captor = ArgumentCaptor.forClass(PaymentHistory.class);
        verify(paymentHistoryRepository, times(1)).save(captor.capture());

        PaymentHistory saved = captor.getValue();
        PlanType actualPlanType = (PlanType) ReflectionTestUtils.getField(saved, "planType");

        assertThat(actualPlanType).isEqualTo(expectedPlanType);
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(PaymentCompletedEvent.class);
    }
}

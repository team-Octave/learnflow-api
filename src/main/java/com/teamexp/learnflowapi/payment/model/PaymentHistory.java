package com.teamexp.learnflowapi.payment.model;

import com.teamexp.learnflowapi.payment.model.constant.PaymentStatus;
import com.teamexp.learnflowapi.payment.model.constant.PlanType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class PaymentHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String userId;
    @Column(nullable = false, unique = true)
    private String orderId;
    private String paymentKey;
    private Long amount;

    @Enumerated(EnumType.STRING)
    private PlanType planType;

    @Enumerated(EnumType.STRING)
    private PaymentStatus status;
    private LocalDateTime createAt;
    private LocalDateTime approvedAt;

    @Builder
    private PaymentHistory(String userId, String orderId, String paymentKey,
                           Long amount, PlanType planType, PaymentStatus status,
                           LocalDateTime approvedAt) {
        this.userId = userId;
        this.orderId = orderId;
        this.paymentKey = paymentKey;
        this.amount = amount;
        this.planType = planType;
        this.status = status;
        this.approvedAt = approvedAt;
    }

    public static PaymentHistory create(String userId, String orderId, String paymentKey,
                                        Long amount, PlanType planType, LocalDateTime approvedAt) {
        return PaymentHistory.builder()
                .userId(userId)
                .orderId(orderId)
                .paymentKey(paymentKey)
                .amount(amount)
                .planType(planType)
                .status(PaymentStatus.DONE)
                .approvedAt(approvedAt)
                .build();
    }
}

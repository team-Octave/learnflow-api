package com.teamexp.learnflowapi.membership.model;

import com.teamexp.learnflowapi.payment.model.constant.PlanType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import java.time.Instant;
import java.time.ZoneId;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Membership {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlanType planType;

    @Column(nullable = false)
    private Instant startAt;

    @Column(nullable = false)
    private Instant expiredAt;

    @CreatedDate
    @Column(updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    private Instant updatedAt;

    public Membership() {

    }

    @Builder
    public Membership(String userId, PlanType planType, Instant startAt, Instant expiredAt, Instant createdAt, Instant updatedAt) {
        this.userId = userId;
        this.planType = planType;
        this.startAt = startAt;
        this.expiredAt = expiredAt;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public static Membership create(String userId, PlanType planType, Instant startAt) {
        return Membership.builder()
                .userId(userId)
                .planType(planType)
                .startAt(startAt)
                .expiredAt(startAt.atZone(ZoneId.systemDefault())
                        .plusMonths(planType.getValue())
                        .toInstant())
                .build();
    }

    public boolean isActive(){
        if(this.expiredAt == null) return false;
        return this.expiredAt.isAfter(Instant.now());
    }
}
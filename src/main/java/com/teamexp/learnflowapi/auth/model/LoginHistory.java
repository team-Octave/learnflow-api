package com.teamexp.learnflowapi.auth.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

/**
 * 로그인 이력 엔티티
 * 로그인 성공 시 비동기로 저장됨
 */
@Getter
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "login_history", indexes = {
        @Index(name = "idx_login_history_user_login", columnList = "user_id, login_at")
})
public class LoginHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "login_history_id")
    private Long id;

    @Column(name = "user_id", nullable = false)
    private String userId;

    @CreatedDate
    @Column(name = "login_at", columnDefinition = "TIMESTAMP", updatable = false, nullable = false)
    private Instant loginAt;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    protected LoginHistory() {
    }

    private LoginHistory(String userId, String ipAddress, String userAgent) {
        this.userId = userId;
        this.ipAddress = ipAddress;
        this.userAgent = userAgent;
    }

    public static LoginHistory create(String userId, String ipAddress, String userAgent) {
        return new LoginHistory(userId, ipAddress, userAgent);
    }
}

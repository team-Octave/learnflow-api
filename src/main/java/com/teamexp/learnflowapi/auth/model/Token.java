package com.teamexp.learnflowapi.auth.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "tokens")
public class Token {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "token_id")
    private Long tokenId;

    @Column(name = "user_id", nullable = false, unique = true)
    private String userId;

    // Token이 생성되는 과정에서 중복이 발생할 수 있기 때문에, unique 제약조건은 제거
    @Column(name = "token", nullable = false)
    private String token;

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private Instant updatedAt;

    protected Token() {}

    private Token(String userId, String token) {
        this.userId = userId;
        this.token = token;
    }

    public static Token createToken(String userId, String token) {
        return new Token(userId, token);
    }

    public void rotate(String token) {
        this.token = token;
    }

    public String getToken() {
        return this.token;
    }


}

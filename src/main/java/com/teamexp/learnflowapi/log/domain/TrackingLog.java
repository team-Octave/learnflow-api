package com.teamexp.learnflowapi.log.domain;


import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class TrackingLog {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String event;

    @Column(length = 2048)
    private String url;

    private String path;

    @Column(length = 2048)
    private String referrer;

    private Long durationMs;

    private Long ts;

    @CreatedDate
    private Instant createdAt;

    @Builder
    public TrackingLog(String event, String url, String path, String referrer, Long durationMs, Long ts) {
        this.event = event;
        this.url = url;
        this.path = path;
        this.referrer = referrer;
        this.durationMs = durationMs;
        this.ts = ts;

    }

}

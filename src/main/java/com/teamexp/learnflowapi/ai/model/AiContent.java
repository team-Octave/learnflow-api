package com.teamexp.learnflowapi.ai.model;

import com.teamexp.learnflowapi.ai.converter.AiContentConverter;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "ai_content",
    indexes = {
        @Index(name = "idx_ai_content_lesson_id", columnList = "lesson_id", unique = true)
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class AiContent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lesson_id", nullable = false, unique = true)
    private Long lessonId;

    @Column(name = "duration_seconds")
    private Integer durationSeconds;

    @Column(name = "duration_formatted", length = 20)
    private String durationFormatted;

    @Column(name = "transcript", columnDefinition = "LONGTEXT")
    private String transcript;

    @Convert(converter = AiContentConverter.FullAnalysisConverter.class)
    @Column(name = "full_analysis", columnDefinition = "JSON")
    private FullAnalysisContent fullAnalysis;

    @Convert(converter = AiContentConverter.class)
    @Column(name = "summary_content", columnDefinition = "JSON")
    private AiSummaryContent summaryContent;

    @Column(name = "model_version", length = 100)
    private String modelVersion;

    @Column(name = "processing_time_seconds")
    private Integer processingTimeSeconds;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    private AiContent(Long lessonId, Integer durationSeconds, String durationFormatted,
                      String transcript, FullAnalysisContent fullAnalysis,
                      AiSummaryContent summaryContent, String modelVersion,
                      Integer processingTimeSeconds) {
        this.lessonId = lessonId;
        this.durationSeconds = durationSeconds;
        this.durationFormatted = durationFormatted;
        this.transcript = transcript;
        this.fullAnalysis = fullAnalysis;
        this.summaryContent = summaryContent;
        this.modelVersion = modelVersion;
        this.processingTimeSeconds = processingTimeSeconds;
    }

    public static AiContent create(Long lessonId, Integer durationSeconds, String durationFormatted,
                                   String transcript, FullAnalysisContent fullAnalysis,
                                   String summary, String modelVersion, Integer processingTimeSeconds) {
        // summary 문자열을 AiSummaryContent로 변환 (기존 호환성)
        AiSummaryContent summaryContent = new AiSummaryContent(summary, null);

        return new AiContent(lessonId, durationSeconds, durationFormatted,
            transcript, fullAnalysis, summaryContent, modelVersion, processingTimeSeconds);
    }
}

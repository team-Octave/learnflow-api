package com.teamexp.learnflowapi.content.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "ReferencedVideos")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class ReferencedVideo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lesson_id", nullable = false, unique = true)
    private Long lessonId;

    @Column(name = "video_url", nullable = false)
    private String videoUrl;

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private Instant updatedAt;

    protected ReferencedVideo() {
    }

    private ReferencedVideo(Long lessonId, String videoUrl) {
        this.lessonId = lessonId;
        this.videoUrl = videoUrl;
    }

    public static ReferencedVideo createReferencedVideo(Long lessonId, String videoUrl) {
        return new ReferencedVideo(lessonId, videoUrl);
    }

    public void changeVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}

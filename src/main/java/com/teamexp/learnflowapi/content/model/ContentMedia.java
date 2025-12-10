package com.teamexp.learnflowapi.content.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "content_media", uniqueConstraints = {
        @UniqueConstraint(name = "uk_content_media_lesson_id", columnNames = "lesson_id")
})
@Getter
@EntityListeners(AuditingEntityListener.class)
public class ContentMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    /*
     * 로컬 파일 시스템의 file_path랑 역할은 비슷하지만, S3에서는 key라는 용어를 사용
     * */
    @Column(name = "file_key", nullable = false)
    private String fileKey;


    @Column(name = "duration_sec")
    private Integer durationSec;

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private Instant updatedAt;

    protected ContentMedia() {}

    private ContentMedia(Long lessonId, String fileKey, Integer durationSec) {
        this.lessonId = lessonId;
        this.fileKey = fileKey;
        this.durationSec = durationSec;
    }

    public static ContentMedia createContentMedia(Long lessonId, String fileKey, Integer durationSec) {
        return new ContentMedia(lessonId, fileKey, durationSec);
    }

    public void changeFile(String fileKey, Integer durationSec) {
        this.fileKey = fileKey;
        this.durationSec = durationSec;
    }
}


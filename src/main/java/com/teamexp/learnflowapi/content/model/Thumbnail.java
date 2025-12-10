package com.teamexp.learnflowapi.content.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.Instant;

@Entity
@Table(name = "thumbnail", uniqueConstraints = {
        @UniqueConstraint(name = "uk_thumbnail_lecture_id", columnNames = "lecture_id")
})
@Getter
public class Thumbnail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lecture_id", nullable = false)
    private Long lectureId;

    @Column(name = "file_key", nullable = false)
    private String fileKey;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private Instant updatedAt;

    protected Thumbnail() {}

    private Thumbnail(Long lectureId, String fileKey, String fileUrl) {
        this.lectureId = lectureId;
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }

    public static Thumbnail createThumbnail(Long lectureId, String fileKey, String fileUrl) {
        return new Thumbnail(lectureId, fileKey, fileUrl);
    }

    public void changeFileKey(String fileKey, String fileUrl) {
        this.fileKey = fileKey;
        this.fileUrl = fileUrl;
    }
}

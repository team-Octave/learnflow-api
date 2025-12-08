package com.teamexp.learnflowapi.content.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "content_media", uniqueConstraints = {
        @UniqueConstraint(name = "uk_content_media_lesson_id", columnNames = "lesson_id")
})
public class ContentMedia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long lessonId;
    private String fileKey;
    private Integer durationSec;

    protected ContentMedia() {}

    public ContentMedia(Long lessonId, String fileKey, Integer durationSec) {
        this.lessonId = lessonId;
        this.fileKey = fileKey;
        this.durationSec = durationSec;
    }

    public Long getId() {
        return id;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public String getFileKey() {
        return fileKey;
    }

    public Integer getDurationSec() {
        return durationSec;
    }

    public void changeFile(String fileKey, Integer durationSec) {
        this.fileKey = fileKey;
        this.durationSec = durationSec;
    }
}


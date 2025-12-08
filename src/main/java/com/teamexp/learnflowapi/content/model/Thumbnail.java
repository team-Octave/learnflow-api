package com.teamexp.learnflowapi.content.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "thumbnail", uniqueConstraints = {
        @UniqueConstraint(name = "uk_thumbnail_lecture_id", columnNames = "lecture_id")
})
public class Thumbnail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long lectureId;
    private String fileKey;

    protected Thumbnail() {}

    public Thumbnail(Long lectureId, String fileKey) {
        this.lectureId = lectureId;
        this.fileKey = fileKey;
    }

    public Long getId() {
        return id;
    }

    public Long getLectureId() {
        return lectureId;
    }

    public String getFileKey() {
        return fileKey;
    }

    public void changeFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
}

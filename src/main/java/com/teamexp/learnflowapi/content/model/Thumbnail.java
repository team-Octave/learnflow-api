package com.teamexp.learnflowapi.content.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.Getter;

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

    protected Thumbnail() {}

    private Thumbnail(Long lectureId, String fileKey) {
        this.lectureId = lectureId;
        this.fileKey = fileKey;
    }

    public static Thumbnail createThumbnail(Long lectureId, String fileKey) {
        return new Thumbnail(lectureId, fileKey);
    }

    public void changeFileKey(String fileKey) {
        this.fileKey = fileKey;
    }
}

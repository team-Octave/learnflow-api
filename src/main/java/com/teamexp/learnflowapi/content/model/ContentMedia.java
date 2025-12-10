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
@Table(name = "content_media", uniqueConstraints = {
        @UniqueConstraint(name = "uk_content_media_lesson_id", columnNames = "lesson_id")
})
@Getter
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


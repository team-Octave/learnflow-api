package com.teamexp.learnflowapi.lecture.model;

import jakarta.persistence.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lectures")
@EntityListeners(AuditingEntityListener.class)
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    // description have to allow setter? or make it immediately at creation? now make it at creation
    @Column
    private String description;

    @Enumerated
    @Column
    private LectureLevel level;

    @CreatedDate
    @Column
    private OffsetDateTime createdAt;

    @Enumerated
    @Column
    private LectureStatus status;

    @Column(name="category_id")
    private Integer categoryId;

    @Column(name="instructor_id") // user_id is UUID string
    private String instructorId;

    @Column(name="thumbnail_id")
    private Long thumbnailId;

    // TODO: add price related VO and embed here
    @Embedded
    private Money price;

    // Aggregate root of Chapter & Lesson
    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Chapter> chapters = new ArrayList<>();

    protected Lecture() {}

    private Lecture(String title, String description, LectureLevel level, LectureStatus status, Integer categoryId, String instructorId) {
        this.title = title;
        this.description = description;
        this.level = level;
        this.status = status;
        this.categoryId = categoryId;
        this.instructorId = instructorId;
    }

    public static Lecture createLecture(String title, String description, LectureLevel level, Integer categoryId, String instructorId) {
        return new Lecture(title, description, level, LectureStatus.UNAVAILABLE, categoryId, instructorId);
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LectureLevel getLevel() {
        return level;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public LectureStatus getStatus() {
        return status;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public String getInstructorId() {
        return instructorId;
    }

    public Long getThumbnailId() {
        return thumbnailId;
    }

    // thumbnailId created after lecture creation, so need setter
    private void setThumbnailId(Long thumbnailId) {
        this.thumbnailId = thumbnailId;
    }

    public void addChapter(Chapter chapter) {
        this.chapters.add(chapter);
        chapter.setLecture(this);
    }

    // After thumbnail is uploaded, call this method to link thumbnail to lecture
    public void addThumbnailLink(Long thumbnailId) {
        setThumbnailId(thumbnailId);
    }

    // In domain level, only unavailable lecture can be made available
    //TODO : In service layer, check at least one chapter and one lesson exist before calling this method
    public boolean canAvailable() {
        return this.status == LectureStatus.UNAVAILABLE;
    }

    public void makeAvailable() {
        this.status = LectureStatus.AVAILABLE;
    }

}

package com.teamexp.learnflowapi.lecture.model;

import jakarta.persistence.*;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "lectures")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Lecture {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    // description have to allow setter? or make it immediately at creation? now make it at creation
    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LectureLevel level;

    @CreatedDate
    @Column(nullable = false)
    private Instant createdAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private LectureStatus status;

    @Column(name = "category_id")
    private Integer categoryId;

    @Column(name = "instructor_id") // user_id is UUID string
    private String instructorId;

    @Column(name = "thumbnail_id")
    private Long thumbnailId;

    // TODO: add price related VO and embed here
    @Embedded
    private Money price;

    // Aggregate root of Chapter & Lesson
    @OneToMany(mappedBy = "lecture", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("chapterOrder ASC")
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
    //TODO : custom exception handling
    public boolean canAvailable() {
        return this.status == LectureStatus.UNAVAILABLE;
    }

    private void validateForAvailable() {
        if (!canAvailable()) {
            throw new IllegalStateException("Lecture is already available.");
        }
        if (chapters.isEmpty()) {
            throw new IllegalStateException("Lecture must have at least one chapter to be made available.");
        }
        boolean hasLesson = chapters.stream()
                .anyMatch(chapter -> !chapter.getLessons().isEmpty());
        if (!hasLesson) {
            throw new IllegalStateException("Lecture must have at least one lesson to be made available.");
        }
    }



    public void makeAvailable() {
        validateForAvailable();
        this.status = LectureStatus.AVAILABLE;
    }

    public List<Chapter> getChapters() {
        return Collections.unmodifiableList(chapters);
    }

    public int getTotalLessonCount() {
        return chapters.stream()
                .mapToInt(chapter -> chapter.getLessons().size())
                .sum();
    }
    

    // Get total count of chapters in this lecture
    public int getTotalChapterCount() {
        return chapters.size();

    }
}

package com.teamexp.learnflowapi.lecture.model;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
@Table(name = "lessons")
public class Lesson {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name="lesson_type")
    private LessonType lessonType;

    @Column(name="lesson_title")
    private String lessonTitle;

    @Column(name="lesson_order")
    private Integer lessonOrder;

    @Column(name="is_free_preview")
    private Boolean isFreePreview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

    protected Lesson() {}

    private Lesson(LessonType lessonType, String lessonTitle, Integer lessonOrder, Boolean isFreePreview) {
        this.lessonType = lessonType;
        this.lessonTitle = lessonTitle;
        this.lessonOrder = lessonOrder;
        this.isFreePreview = isFreePreview;
    }

    public static Lesson createLesson(LessonType lessonType, String lessonTitle, Integer lessonOrder, Boolean isFreePreview) {
        return new Lesson(lessonType, lessonTitle, lessonOrder, isFreePreview);
    }

    void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }
}

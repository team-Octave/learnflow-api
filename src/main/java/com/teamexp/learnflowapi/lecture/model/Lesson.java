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
    @Column(name = "lesson_type")
    private LessonType lessonType;

    @Column(name="lesson_title", nullable = false)
    private String lessonTitle;

    @Column(name = "lesson_order", nullable = false)
    private Integer lessonOrder;

    @Column(name="is_free_preview", nullable = false) // need to set default value to false
    private Boolean isFreePreview;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chapter_id")
    private Chapter chapter;

//    @Column(name="next_lesson_id")
//    private Long nextLessonId;

    protected Lesson() {}

    private Lesson(LessonType lessonType, String lessonTitle, Integer lessonOrder, Boolean isFreePreview) {
        this.lessonType = lessonType;
        this.lessonTitle = lessonTitle;
        this.lessonOrder = lessonOrder;
        this.isFreePreview = (isFreePreview != null ? isFreePreview : false);
    }
    public static Lesson createLesson(LessonType lessonType, String lessonTitle, Integer lessonOrder, Boolean isFreePreview) {
        return new Lesson(lessonType, lessonTitle, lessonOrder, (isFreePreview != null ? isFreePreview : false));
    }

    void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }
}

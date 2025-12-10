package com.teamexp.learnflowapi.lecture.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


@Entity
@Getter
@Table(name = "chapters")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "chapter_order", nullable = false)
    private Integer chapterOrder;

    @Column(name = "chapter_title", nullable = false)
    private String chapterTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id", nullable = false)
    private Lecture lecture;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("lessonOrder ASC")
    private List<Lesson> lessons = new ArrayList<>();

    protected Chapter() {}

    private Chapter(String chapterTitle, Integer chapterOrder) {
        this.chapterOrder = chapterOrder;
        this.chapterTitle = chapterTitle;
    }

    public static Chapter createChapter(String chapterTitle, Integer chapterOrder) {
        return new Chapter(chapterTitle,chapterOrder);
    }

    void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }

    public void addLesson(Lesson lesson) {
        this.lessons.add(lesson);
        lesson.setChapter(this);
    }

    public List<Lesson> getLessons() {
        return Collections.unmodifiableList(lessons);
    }

    public Lesson findByLessonId(Long lessonId) {
        return lessons.stream()
            .filter(l -> l.getId().equals(lessonId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("레슨을 찾을 수 없습니다: " + lessonId));

    }

}

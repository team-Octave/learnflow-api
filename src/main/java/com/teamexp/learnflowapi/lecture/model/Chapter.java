package com.teamexp.learnflowapi.lecture.model;

import jakarta.persistence.*;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "chapters")
public class Chapter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="chapter_order")
    private Integer chapterOrder;

    @Column(name = "chapter_title")
    private String chapterTitle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "lecture_id")
    private Lecture lecture;

    @OneToMany(mappedBy = "chapter", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Lesson> lessons = new ArrayList<>();

    protected Chapter() {}

    private Chapter(Integer chapterOrder, String chapterTitle) {
        this.chapterOrder = chapterOrder;
        this.chapterTitle = chapterTitle;
    }

    public static Chapter createChapter(Integer chapterOrder, String chapterTitle) {
        return new Chapter(chapterOrder, chapterTitle);
    }

    void setLecture(Lecture lecture) {
        this.lecture = lecture;
    }

    public void addLesson(Lesson lesson) {
        this.lessons.add(lesson);
        lesson.setChapter(this);
    }

}

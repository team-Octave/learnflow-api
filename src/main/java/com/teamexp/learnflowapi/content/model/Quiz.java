package com.teamexp.learnflowapi.content.model;

import jakarta.persistence.*;

@Entity
@Table(name = "lesson_quiz")
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long lessonId;
    private int orderIndex;
    private String question;
    private boolean correct;

    protected Quiz() {}

    public Quiz(Long lessonId, int orderIndex, String question, boolean correct) {
        this.lessonId = lessonId;
        this.orderIndex = orderIndex;
        this.question = question;
        this.correct = correct;
    }

    public Long getId() {
        return id;
    }

    public Long getLessonId() {
        return lessonId;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public String getQuestion() {
        return question;
    }

    public boolean isCorrect() {
        return correct;
    }


    public void update(String question, boolean correct, int orderIndex) {
        this.question = question;
        this.correct = correct;
        this.orderIndex = orderIndex;
    }
}

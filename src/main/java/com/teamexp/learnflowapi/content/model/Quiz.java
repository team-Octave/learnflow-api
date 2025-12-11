package com.teamexp.learnflowapi.content.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.Instant;

@Entity
@Table(name = "quizzes")
@Getter
@EntityListeners(AuditingEntityListener.class)
public class Quiz {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lesson_id", nullable = false)
    private Long lessonId;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    @Column(name = "question", length = 1000)
    private String question;

    @Column(name = "correct", nullable = false)
    private Boolean correct;

    @CreatedDate
    @Column(name = "created_at", columnDefinition = "TIMESTAMP", updatable = false, nullable = false)
    private Instant createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", columnDefinition = "TIMESTAMP", nullable = false)
    private Instant updatedAt;

    protected Quiz() {
    }

    private Quiz(Long lessonId, Integer orderIndex, String question, Boolean correct) {
        this.lessonId = lessonId;
        this.orderIndex = orderIndex;
        this.question = question;
        this.correct = correct;
    }

    public static Quiz createQuiz(Long lessonId, Integer orderIndex, String question, Boolean correct) {
        return new Quiz(lessonId, orderIndex, question, correct);
    }

    public void update(String question, Boolean correct) {
        this.question = question;
        this.correct = correct;
    }

    public void changeOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

}

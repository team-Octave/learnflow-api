package com.teamexp.learnflowapi.lecture.model;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

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

    @Column(name="video_url")
    private String videoUrl;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC")
    private Set<Quiz> quizzes = new LinkedHashSet<>();

//    @Column(name="next_lesson_id")
//    private Long nextLessonId;

    protected Lesson() {}

    private Lesson(LessonType lessonType, String lessonTitle, Integer lessonOrder, Boolean isFreePreview, String videoUrl) {
        this.lessonType = lessonType;
        this.lessonTitle = lessonTitle;
        this.lessonOrder = lessonOrder;
        this.isFreePreview = (isFreePreview != null ? isFreePreview : false);
        this.videoUrl = videoUrl;
    }
    public static Lesson createLesson(LessonType lessonType, String lessonTitle, Integer lessonOrder, Boolean isFreePreview, String videoUrl) {
        return new Lesson(lessonType, lessonTitle, lessonOrder, (isFreePreview != null ? isFreePreview : false), videoUrl);
    }

    public void updateTitle(String lessonTitle) {
        this.lessonTitle = lessonTitle;
    }

    public void updateFreePreview(Boolean isFreePreview) {
        this.isFreePreview = (isFreePreview != null ? isFreePreview : false);
    }

    public void updateVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }

    public void changeOrder(Integer lessonOrder) {
        this.lessonOrder = lessonOrder;
    }

    void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }

    public void addQuiz(Quiz quiz) {
        this.quizzes.add(quiz);
        quiz.setLesson(this);
    }

    /**
     * Replace quizzes under this lesson while keeping JPA relationship consistent.
     *
     * <p>NOTE: With orphanRemoval=true, removing from collection will delete orphan rows on flush.
     */
    public void bindQuizzes(List<Quiz> quizzes) {
        this.quizzes.clear();
        if (quizzes == null || quizzes.isEmpty()) {
            return;
        }
        quizzes.forEach(this::addQuiz);
    }

    /**
     * Safe read-only view of quizzes ordered by {@code orderIndex ASC}.
     */
    public List<Quiz> unpackingQuizzes() {
        return this.quizzes.stream()
            .sorted(Comparator.comparing(Quiz::getOrderIndex))
            .toList();
    }

    public void removeQuizById(Long quizId) {
        if (quizId == null) {
            return;
        }
        this.quizzes.removeIf(q -> quizId.equals(q.getId()));
    }
}

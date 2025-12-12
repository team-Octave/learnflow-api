package com.teamexp.learnflowapi.lecture.model;

import com.teamexp.learnflowapi.content.model.Quiz;
import jakarta.persistence.*;
import lombok.Getter;

import java.util.List;

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

    @Transient
    private List<Quiz> quizzes;

    /**
     * Associates a list of Quiz objects with this Lesson by storing them in the transient quizzes field.
     *
     * @param quizzes the quizzes to attach to this lesson; may be {@code null} to clear previously bound quizzes
     */
    public void bindQuizzes(List<Quiz> quizzes) {
        this.quizzes = quizzes;
    }

    /**
     * Returns the quizzes currently bound to this lesson.
     *
     * @return the list of bound Quiz objects, or `null` if no quizzes have been bound
     */
    public List<Quiz> unpackingQuizzes() {
        return this.quizzes;
    }

//    @Column(name="next_lesson_id")
/**
 * Protected no-argument constructor for JPA and other frameworks that require a default constructor.
 *
 * <p>Intended for use by the persistence provider; do not call directly in application code.</p>
 */

    protected Lesson() {}

    /**
     * Create a Lesson instance with the specified properties.
     *
     * @param lessonType    the lesson's type (stored as a string in the database)
     * @param lessonTitle   the title of the lesson; required (not null)
     * @param lessonOrder   the display/order index of the lesson within its chapter
     * @param isFreePreview whether the lesson is available as a free preview; if `null`, defaults to `false`
     * @param videoUrl      optional URL of the lesson video; may be null
     */
    private Lesson(LessonType lessonType, String lessonTitle, Integer lessonOrder, Boolean isFreePreview, String videoUrl) {
        this.lessonType = lessonType;
        this.lessonTitle = lessonTitle;
        this.lessonOrder = lessonOrder;
        this.isFreePreview = (isFreePreview != null ? isFreePreview : false);
        this.videoUrl = videoUrl;
    }
    /**
     * Create a Lesson with the specified properties.
     *
     * @param lessonType the lesson's type
     * @param lessonTitle the lesson's title
     * @param lessonOrder the lesson's order within its chapter
     * @param isFreePreview whether the lesson is a free preview; if null, treated as false
     * @param videoUrl optional URL of the lesson's video; may be null
     * @return the created Lesson instance
     */
    public static Lesson createLesson(LessonType lessonType, String lessonTitle, Integer lessonOrder, Boolean isFreePreview, String videoUrl) {
        return new Lesson(lessonType, lessonTitle, lessonOrder, (isFreePreview != null ? isFreePreview : false), videoUrl);
    }

    /**
     * Associates this lesson with the given chapter.
     *
     * @param chapter the Chapter to associate with this lesson, or `null` to clear the association
     */
    void setChapter(Chapter chapter) {
        this.chapter = chapter;
    }
}
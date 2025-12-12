package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.model.LessonType;

import java.util.List;


public record LessonResponse(
    Long id,
    String lessonTitle,
    String lessonTypeDisplayName,
    Integer lessonOrder,
    Boolean isFreePreview,
    String videoUrl,
    List<QuizQuestionResponse> quizQuestions

) {
    public record QuizQuestionResponse(
        Long id,
        String question,
        Integer orderIndex,
        Boolean correct
    ) {
    }

    /**
     * Create a LessonResponse for a lesson that has no quiz questions.
     *
     * @param lessonId              the lesson's unique identifier
     * @param lessonTitle           the lesson's title
     * @param lessonTypeDisplayName the display name of the lesson type
     * @param order                 the lesson's order index within its parent sequence
     * @param isFreePreview         whether the lesson is available as a free preview
     * @param videoUrl              the lesson's video URL, or null if none
     * @return                      a LessonResponse populated with the provided values and no quiz questions
     */
    public static LessonResponse withoutQuiz(
        Long lessonId,
        String lessonTitle,
        String lessonTypeDisplayName,
        Integer order,
        Boolean isFreePreview,
        String videoUrl
    ) {
        return new LessonResponse(
            lessonId,
            lessonTitle,
            lessonTypeDisplayName,
            order,
            isFreePreview,
            videoUrl,
            null
        );
    }

    /**
     * Create a LessonResponse for a lesson that provides quiz questions but no video.
     *
     * @param lessonId               the lesson's identifier
     * @param lessonTitle            the lesson's title
     * @param lessonTypeDisplayName  display name for the lesson type
     * @param order                  the lesson's order/index within its sequence
     * @param isFreePreview          whether the lesson is available as a free preview
     * @param quizQuestions          list of quiz question responses associated with the lesson
     * @return                       a LessonResponse with `videoUrl` set to null and `quizQuestions` set to the provided list
     */
    public static LessonResponse withoutVideo(
        Long lessonId,
        String lessonTitle,
        String lessonTypeDisplayName,
        Integer order,
        Boolean isFreePreview,
        List<LessonResponse.QuizQuestionResponse> quizQuestions
    ) {
        return new LessonResponse(
            lessonId,
            lessonTitle,
            lessonTypeDisplayName,
            order,
            isFreePreview,
            null,
            quizQuestions
        );
    }


}
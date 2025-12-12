package com.teamexp.learnflowapi.lecture.dto.response;

import java.util.List;

public record LectureFullCreateResponse(
    Long lectureId,
    String title,
    String description,
    Integer categoryId,
    String level, // Display name of LectureLevel
    List<ChapterResponse> chapters,
    String instructorNickname
) {
    public record ChapterResponse(
        Long chapterId,
        String chapterTitle,
        Integer order,
        List<LessonResponse> lessons
    ) {
    }

    // video lesson or quiz lesson response
    public record LessonResponse(
        Long lessonId,
        String lessonTitle,
        Integer order,
        String lessonType, // Display name of LessonType
        Boolean isFreePreview,
        String videoUrl,
        List<QuizQuestionResponse> quizQuestions
    ) {
        public record QuizQuestionResponse(
            Long questionId,
            String question,
            Integer questionOrder,
            Boolean correct
        ) {
        }

        public static LessonResponse withoutQuiz(
            Long lessonId,
            String lessonTitle,
            Integer order,
            String lessonType,
            Boolean isFreePreview,
            String videoUrl
        ) {
            return new LessonResponse(
                lessonId,
                lessonTitle,
                order,
                lessonType,
                isFreePreview,
                videoUrl,
                null
            );
        }

        public static LessonResponse withoutVideo(
            Long lessonId,
            String lessonTitle,
            Integer order,
            String lessonType,
            Boolean isFreePreview,
            List<QuizQuestionResponse> quizQuestions
        ) {
            return new LessonResponse(
                lessonId,
                lessonTitle,
                order,
                lessonType,
                isFreePreview,
                null,
                quizQuestions
            );
        }
    }
}

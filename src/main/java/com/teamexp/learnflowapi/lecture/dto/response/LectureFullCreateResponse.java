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

        /**
         * Create a LessonResponse that includes a video but no quiz questions.
         *
         * @param lessonId      the lesson's identifier
         * @param lessonTitle   the lesson's title
         * @param order         the lesson's ordering index within its chapter
         * @param lessonType    the display name of the lesson's type
         * @param isFreePreview whether the lesson is available as a free preview
         * @param videoUrl      the URL of the lesson's video (may be null)
         * @return              a LessonResponse populated with the provided values and `quizQuestions` set to `null`
         */
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

        /**
         * Create a LessonResponse that contains the provided quiz questions and explicitly has no video URL.
         *
         * @param lessonId       the lesson identifier
         * @param lessonTitle    the lesson title
         * @param order          the lesson's order within its chapter
         * @param lessonType     the display name of the lesson type
         * @param isFreePreview  whether the lesson is available as a free preview
         * @param quizQuestions  the list of quiz questions for the lesson (may be null)
         * @return               a LessonResponse with `videoUrl` set to null and `quizQuestions` set to the provided list
         */
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
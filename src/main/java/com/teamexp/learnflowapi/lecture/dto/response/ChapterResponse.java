package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.Chapter;
import com.teamexp.learnflowapi.lecture.model.LessonType;


import java.util.List;
import java.util.stream.Collectors;

public record ChapterResponse(
    Long id,
    String chapterTitle,
    Integer chapterOrder,
    int lessonCount,
    List<LessonResponse> lessons
) {
    /**
     * Create a ChapterResponse DTO from a Chapter domain model.
     *
     * The returned ChapterResponse contains id, title, order, lesson count, and a list of LessonResponse objects.
     * Lessons of type QUIZ are mapped to LessonResponse instances that include a list of QuizQuestionResponse objects;
     * other lesson types are mapped to LessonResponse instances that include the lesson's videoUrl.
     *
     * @param chapter the source Chapter to convert
     * @return a ChapterResponse populated from the chapter's properties and converted lessons
     */
    public static ChapterResponse from(Chapter chapter) {
        return new ChapterResponse(
            chapter.getId(),
            chapter.getChapterTitle(),
            chapter.getChapterOrder(),
            chapter.getLessons().size(),
            chapter.getLessons().stream()
                .map(lesson -> {
                    if (lesson.getLessonType() == LessonType.QUIZ) {
                        return LessonResponse.withoutVideo(
                            lesson.getId(),
                            lesson.getLessonTitle(),
                            lesson.getLessonType().getDisplayName(),
                            lesson.getLessonOrder(),
                            lesson.getIsFreePreview(),
                            lesson.unpackingQuizzes().stream()
                                .map(quizQuestion -> new LessonResponse.QuizQuestionResponse(
                                    quizQuestion.getId(),
                                    quizQuestion.getQuestion(),
                                    quizQuestion.getOrderIndex(),
                                    quizQuestion.getCorrect()
                                ))
                                .collect(Collectors.toList())
                        );
                    } else {
                        return LessonResponse.withoutQuiz(
                            lesson.getId(),
                            lesson.getLessonTitle(),
                            lesson.getLessonType().getDisplayName(),
                            lesson.getLessonOrder(),
                            lesson.getIsFreePreview(),
                            lesson.getVideoUrl()
                        );
                    }
                })
                .collect(Collectors.toList())
        );
    }
}
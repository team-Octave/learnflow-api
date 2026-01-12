package com.teamexp.learnflowapi.lecture.dto.response;

import com.teamexp.learnflowapi.lecture.model.Chapter;
import com.teamexp.learnflowapi.lecture.model.LessonType;
import com.teamexp.learnflowapi.lecture.model.Quiz;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public record ChapterResponse(
    Long id,
    String chapterTitle,
    Integer chapterOrder,
    int lessonCount,
    List<LessonResponse> lessons
) {
    public static ChapterResponse from(Chapter chapter) {
        return new ChapterResponse(
            chapter.getId(),
            chapter.getChapterTitle(),
            chapter.getChapterOrder(),
            chapter.getLessons().size(),
            chapter.getLessons().stream()
                .map(lesson -> {
                    if (lesson.getLessonType() == LessonType.QUIZ) {
                        List<LessonResponse.QuizQuestionResponse> quizQuestions = lesson.getQuizzes().stream()
                            .sorted(Comparator.comparing(Quiz::getOrderIndex))
                            .map(quizQuestion -> new LessonResponse.QuizQuestionResponse(
                                quizQuestion.getId(),
                                quizQuestion.getQuestion(),
                                quizQuestion.getOrderIndex(),
                                quizQuestion.getCorrect()
                            ))
                            .collect(Collectors.toList());

                        return LessonResponse.withoutVideo(
                            lesson.getId(),
                            lesson.getLessonTitle(),
                            lesson.getLessonType().getDisplayName(),
                            lesson.getLessonOrder(),
                            lesson.getIsFreePreview(),
                            quizQuestions
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
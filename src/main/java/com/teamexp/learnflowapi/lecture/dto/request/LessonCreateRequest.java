package com.teamexp.learnflowapi.lecture.dto.request;

import java.util.List;

import com.teamexp.learnflowapi.lecture.model.LessonType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record LessonCreateRequest(
    @NotBlank(message = "레슨 제목은 필수입니다.")
    @Size(max = 200, message = "레슨 제목은 200자를 초과할 수 없습니다.")
    String lessonTitle,

    @NotNull(message = "레슨 타입은 필수입니다.")
    LessonType lessonType,

    Boolean isFreePreview,

    String videoUrl, // videoUrl is nullable if lessonType is QUIZ

    List<QuizQuestion> quizQuestions // quizQuestions is nullable if lessonType is VIDEO
    ) {
        public record QuizQuestion(
            @NotBlank(message = "퀴즈 질문은 필수 입력 값입니다.")
            String question,

            @NotNull(message = "퀴즈 질문 순서는 필수 입력 값입니다.")
            Integer questionOrder,

            @NotNull(message = "정답 값은 필수 입력 값입니다.")
            Boolean correct
        ){
        }
}


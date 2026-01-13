package com.teamexp.learnflowapi.lecture.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Lesson(퀴즈 타입) 하위의 퀴즈 목록을 "전체 교체"하는 요청 DTO.
 *
 * <p>PUT semantics: 동일 payload로 여러 번 요청해도 결과가 같도록 Service에서 교체 방식으로 처리한다.
 */
public record LessonQuizReplaceRequest(
    @NotNull(message = "퀴즈 질문 목록은 필수 입력 값입니다.")
    @Size(min = 1, message = "최소 1개 이상의 퀴즈 질문을 추가해주세요.")
    @Valid
    List<QuizQuestionRequest> quizQuestions
) {
    public record QuizQuestionRequest(
        @NotBlank(message = "퀴즈 질문은 필수 입력 값입니다.")
        String question,

        @NotNull(message = "퀴즈 질문 순서는 필수 입력 값입니다.")
        Integer questionOrder,

        @NotNull(message = "정답 값은 필수 입력 값입니다.")
        Boolean correct
    ) {
    }
}



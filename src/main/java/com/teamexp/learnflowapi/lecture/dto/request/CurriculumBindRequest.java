package com.teamexp.learnflowapi.lecture.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * 챕터/레슨의 order를 "확정"하기 위한 batch 요청 DTO.
 *
 * <p>요청은 ID와 order만 포함하며, Service에서 lecture aggregate 내 존재/소유권/중복 등을 검증한 뒤 반영한다.
 */
public record CurriculumBindRequest(
    @NotNull(message = "챕터 목록은 필수 입력 값입니다.")
    @Size(min = 1, message = "최소 1개 이상의 챕터가 필요합니다.")
    @Valid
    List<ChapterOrderRequest> chapters
) {
    public record ChapterOrderRequest(
        @NotNull(message = "chapterId는 필수 입력 값입니다.")
        Long chapterId,

        @NotNull(message = "chapter order는 필수 입력 값입니다.")
        Integer order,

        @NotNull(message = "레슨 목록은 필수 입력 값입니다.")
        @Size(min = 1, message = "최소 1개 이상의 레슨이 필요합니다.")
        @Valid
        List<LessonOrderRequest> lessons
    ) {
    }

    public record LessonOrderRequest(
        @NotNull(message = "lessonId는 필수 입력 값입니다.")
        Long lessonId,

        @NotNull(message = "lesson order는 필수 입력 값입니다.")
        Integer order
    ) {
    }
}



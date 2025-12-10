package com.teamexp.learnflowapi.lecture.dto.request;

import com.teamexp.learnflowapi.lecture.model.LectureLevel;
import com.teamexp.learnflowapi.lecture.model.LessonType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record LectureFullCreateRequest(
    @NotNull(message = "챕터는 필수 입력 값입니다.")
    @Size(min = 1, message = "최소 1개 이상의 챕터를 추가해주세요.")
    @Valid
    List<ChapterRequest> chapters
) {
    public record ChapterRequest(
        @NotBlank(message = "챕터 제목은 필수 입력 값입니다.")
        @Size(max = 100, message = "챕터 제목은 100자 이내로 작성해주세요.")
        String chapterTitle,

        @NotNull(message = "레슨은 필수 입력 값입니다.")
        @Size(min = 1, message = "최소 1개 이상의 레슨을 추가해주세요.")
        @Valid
        List<LessonRequest> lessons
    ) {
    }

    public record LessonRequest(
        @NotBlank(message = "레슨 제목은 필수 입력 값입니다.")
        @Size(max = 100, message = "레슨 제목은 100자 이내로 작성해주세요.")
        String lessonTitle,

        @NotBlank

        String lessonType,

        Boolean isFreePreview
    ) {
    }
}

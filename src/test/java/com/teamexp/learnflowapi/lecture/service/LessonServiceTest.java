package com.teamexp.learnflowapi.lecture.service;

import com.teamexp.learnflowapi.content.service.ContentMediaService;
import com.teamexp.learnflowapi.enrollment.repository.EnrollmentRepository;
import com.teamexp.learnflowapi.lecture.dto.request.LessonCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LessonUpdateRequest;
import com.teamexp.learnflowapi.lecture.exception.LessonQuizCountInvalidException;
import com.teamexp.learnflowapi.lecture.exception.LessonVideoUrlInvalidException;
import com.teamexp.learnflowapi.lecture.model.Chapter;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.model.LessonType;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LessonRepository;
import com.teamexp.learnflowapi.lecture.repository.QuizRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LessonServiceTest {

    @InjectMocks
    LessonService lessonService;

    @Mock
    LessonRepository lessonRepository;

    @Mock
    LectureRepository lectureRepository;

    @Mock
    QuizRepository quizRepository;

    @Mock
    LectureAccessValidator lectureAccessValidator;

    @Mock
    ContentMediaService contentMediaService;

    @Mock
    EnrollmentRepository enrollmentRepository;

    @Test
    @DisplayName("lesson 생성 실패 - VIDEO + mediaId 없음")
    void tc33_lesson_create_fail_video_no_mediaId() {
        Long lectureId = 1L;
        Long chapterId = 10L;
        String instructorId = "instructor123";

        Lecture lecture = mock(Lecture.class);
        given(lectureRepository.findByIdWithChaptersAndLessons(lectureId)).willReturn(Optional.of(lecture));
        given(lecture.isDeleteFlag()).willReturn(false);
        given(lecture.getStatus()).willReturn(LectureStatus.SUBMITTED); // AVAILABLE만 막힘
        doNothing().when(lectureAccessValidator).validateOwnership(lecture, instructorId);

        LessonCreateRequest request = mock(LessonCreateRequest.class);
        given(request.lessonType()).willReturn(LessonType.VIDEO);
        given(request.mediaId()).willReturn(null);

        assertThatThrownBy(() -> lessonService.addLesson(lectureId, chapterId, request, instructorId))
                .isInstanceOf(LessonVideoUrlInvalidException.class);

        verify(lecture, never()).findByChapterId(anyLong());
        verify(contentMediaService, never()).bindMediaToLesson(anyLong(), anyLong());
        verify(lessonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lesson 생성 실패 - QUIZ 문제 수 초과")
    void tc34_lesson_create_fail_quiz_over_limit() {
        Long lectureId = 1L;
        Long chapterId = 10L;
        String instructorId = "instructor123";

        Lecture lecture = mock(Lecture.class);
        given(lectureRepository.findByIdWithChaptersAndLessons(lectureId)).willReturn(Optional.of(lecture));
        given(lecture.isDeleteFlag()).willReturn(false);
        given(lecture.getStatus()).willReturn(LectureStatus.SUBMITTED); // AVAILABLE만 막힘
        doNothing().when(lectureAccessValidator).validateOwnership(lecture, instructorId);

        LessonCreateRequest request = mock(LessonCreateRequest.class);
        given(request.lessonType()).willReturn(LessonType.QUIZ);
        given(request.mediaId()).willReturn(null);
        given(request.quizQuestions()).willReturn(java.util.Collections.nCopies(11, null)); // 11개 문제

        assertThatThrownBy(() -> lessonService.addLesson(lectureId, chapterId, request, instructorId))
                .isInstanceOf(LessonQuizCountInvalidException.class);

        verify(lecture, never()).findByChapterId(anyLong());
        verify(lessonRepository, never()).save(any());
    }

    @Test
    @DisplayName("Lesson 수정 실패 - QUIZ 레슨에 mediaId 업데이트 시도")
    void tc35_lesson_update_fail_quiz_with_mediaId() {
        Long lectureId = 1L;
        Long lessonId = 100L;
        String instructorId = "instructor123";

        Lecture lecture = mock(Lecture.class);
        given(lectureRepository.findByIdWithChaptersAndLessons(lectureId))
                .willReturn(Optional.of(lecture));
        given(lecture.isDeleteFlag()).willReturn(false);
        given(lecture.getStatus()).willReturn(LectureStatus.SUBMITTED);
        doNothing().when(lectureAccessValidator).validateOwnership(lecture, instructorId);

        Chapter chapter = mock(Chapter.class);
        Lesson lesson = mock(Lesson.class);

        given(lesson.getId()).willReturn(lessonId);
        given(lesson.getLessonType()).willReturn(LessonType.QUIZ);

        given(chapter.getLessons()).willReturn(Set.of(lesson));
        given(chapter.findByLessonId(lessonId)).willReturn(lesson);
        given(lecture.getChapters()).willReturn(Set.of(chapter));

        LessonUpdateRequest updateRequest = mock(LessonUpdateRequest.class);
        given(updateRequest.mediaId()).willReturn(123L); // 핵심 조건

        assertThatThrownBy(() -> lessonService.updateLesson(lectureId, lessonId, updateRequest, instructorId))
                .isInstanceOf(LessonVideoUrlInvalidException.class);

        verify(contentMediaService, never()).bindMediaToLesson(anyLong(), anyLong());
        verify(lessonRepository, never()).save(any());
    }

}

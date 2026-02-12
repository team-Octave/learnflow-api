package com.teamexp.learnflowapi.lecture.service;

import com.teamexp.learnflowapi.admin.repository.ApprovalRepository;
import com.teamexp.learnflowapi.content.repository.ThumbnailRepository;
import com.teamexp.learnflowapi.lecture.dto.request.LectureBaseUpdateRequest;
import com.teamexp.learnflowapi.lecture.exception.LectureCannotUpdateException;
import com.teamexp.learnflowapi.lecture.exception.LectureDeleteBlockedException;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.lecture.repository.LectureStatisticRepository;
import com.teamexp.learnflowapi.lecture.repository.QuizRepository;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

    @InjectMocks
    LectureService lectureService;

    @Mock
    LectureRepository lectureRepository;

    @Mock
    LectureStatisticRepository lectureStatisticRepository;

    @Mock
    ThumbnailRepository thumbnailRepository;

    @Mock
    UserRepository userRepository;

    @Mock
    QuizRepository quizRepository;

    @Mock
    LectureAccessValidator lectureAccessValidator;

    @Mock
    ApprovalRepository approvalRepository;

    @Test
    @DisplayName("Lecture 수정 실패 - 이미 발행됨(AVAILABLE)")
    void tc31_lecture_update_fail_already_published() {
        Long lectureId = 1L;
        String instructorId = "instructor123";
        LectureBaseUpdateRequest updateRequest = mock(LectureBaseUpdateRequest.class);

        Lecture lecture = mock(Lecture.class);
        given(lectureRepository.findById(lectureId)).willReturn(Optional.of(lecture));
        given(lecture.isDeleteFlag()).willReturn(false);
        given(lecture.getStatus()).willReturn(LectureStatus.AVAILABLE);

        doNothing().when(lectureAccessValidator).validateOwnership(lecture, instructorId);

        assertThatThrownBy(() -> lectureService.updateLecture(lectureId, updateRequest, instructorId))
                .isInstanceOf(LectureCannotUpdateException.class);

        verify(lectureRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Lecture 삭제 실패 - 발행 상태(AVAILABLE)")
    void tc32_lecture_delete_fail_published() {
        Long lectureId = 1L;
        String instructorId = "instructor123";

        Lecture lecture = mock(Lecture.class);
        given(lectureRepository.findByIdWithChapters(lectureId)).willReturn(Optional.of(lecture));
        given(lecture.isDeleteFlag()).willReturn(false);
        given(lecture.getStatus()).willReturn(LectureStatus.AVAILABLE);

        doNothing().when(lectureAccessValidator).validateOwnership(lecture, instructorId);

        assertThatThrownBy(() -> lectureService.deleteLecture(lectureId, instructorId))
                .isInstanceOf(LectureDeleteBlockedException.class);

        verify(lectureRepository, never()).save(any());
        verify(userRepository, never()).findById(any());
    }
}

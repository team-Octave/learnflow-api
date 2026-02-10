package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.dto.ApprovalUpdateResponse;
import com.teamexp.learnflowapi.admin.dto.request.ApprovalStatus;
import com.teamexp.learnflowapi.admin.dto.request.ApprovalUpdateRequest;
import com.teamexp.learnflowapi.admin.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.admin.model.Approval;
import com.teamexp.learnflowapi.admin.model.ApprovalRejectType;
import com.teamexp.learnflowapi.admin.repository.ApprovalRepository;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.repository.LectureAdminRepository;
import com.teamexp.learnflowapi.lecture.service.LectureAdminService;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ApprovalServiceTest {

    @InjectMocks
    ApprovalService approvalService;

    @Mock
    UserRepository userRepository;

    @Mock
    LectureAdminService lectureAdminService;

    @Mock
    LectureAdminRepository lectureAdminRepository;

    @Mock
    ApprovalRepository approvalRepository;

    @Captor
    ArgumentCaptor<Approval> approvalCaptor;

    @Test
    @DisplayName("승인/반려 실패 - 강의 없음")
    void tc15_approval_update_fail_when_lecture_not_found() {
        Long lectureId = 9999L;
        ApprovalUpdateRequest request = new ApprovalUpdateRequest(ApprovalStatus.APPROVED, List.of(), null);

        when(lectureAdminRepository.findById(lectureId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> approvalService.updateApproval(lectureId, request))
                .isInstanceOf(LectureNotFoundException.class);

        verify(approvalRepository, never()).saveAndFlush(any());
        verify(lectureAdminService, never()).allowPublishLecture(anyLong());
        verify(lectureAdminService, never()).notAllowPublishLecture(anyLong());
    }

    @Test
    @DisplayName("승인 성공 - 저장 + publish 호출")
    void tc16_approval_update_approve_success() {
        Long lectureId = 1L;
        Lecture lecture = mock(Lecture.class);
        ApprovalUpdateRequest request = new ApprovalUpdateRequest(ApprovalStatus.APPROVED, List.of(), null);

        when(lectureAdminRepository.findById(lectureId)).thenReturn(Optional.of(lecture));

        when(approvalRepository.saveAndFlush(any(Approval.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalUpdateResponse approvalUpdateResponse = approvalService.updateApproval(lectureId, request);

        verify(approvalRepository, times(1)).saveAndFlush(approvalCaptor.capture());
        verify(lectureAdminService, times(1)).allowPublishLecture(lectureId);
        verify(lectureAdminService, never()).notAllowPublishLecture(anyLong());

        Approval saved = approvalCaptor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved.getLectureId()).isEqualTo(lectureId);

        assertThat(approvalUpdateResponse.lectureId()).isEqualTo(lectureId);
        assertThat(approvalUpdateResponse.status()).isEqualTo("PUBLISHED");
    }

    @Test
    @DisplayName("반려 성공 - 저장 + reject 호출")
    void tc17_approval_update_reject_success() {
        Long lectureId = 2L;
        Lecture lecture = mock(Lecture.class);
        List<ApprovalRejectType> rejectTypes = List.of(ApprovalRejectType.CONTENT_QUALITY_LOW);
        String reason = "카테고리/사유가 맞지 않습니다.";

        ApprovalUpdateRequest request = new ApprovalUpdateRequest(
                ApprovalStatus.REJECTED,
                rejectTypes,
                reason
        );

        when(lectureAdminRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
        when(approvalRepository.saveAndFlush(any(Approval.class))).thenAnswer(inv -> inv.getArgument(0));

        ApprovalUpdateResponse response = approvalService.updateApproval(lectureId, request);

        verify(approvalRepository, times(1)).saveAndFlush(approvalCaptor.capture());
        verify(lectureAdminService, times(1)).notAllowPublishLecture(lectureId);
        verify(lectureAdminService, never()).allowPublishLecture(anyLong());

        Approval saved = approvalCaptor.getValue();
        assertThat(saved).isNotNull();
        assertThat(saved.getLectureId()).isEqualTo(lectureId);

        assertThat(response.lectureId()).isEqualTo(lectureId);
        assertThat(response.status()).isEqualTo("REJECTED");
    }
}

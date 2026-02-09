package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.dto.ApprovalDetailResponse;
import com.teamexp.learnflowapi.admin.dto.ApprovalDto;
import com.teamexp.learnflowapi.admin.dto.ApprovalUpdateResponse;
import com.teamexp.learnflowapi.admin.dto.ApprovalsResponse;
import com.teamexp.learnflowapi.admin.dto.request.ApprovalStatus;
import com.teamexp.learnflowapi.admin.dto.request.ApprovalUpdateRequest;
import com.teamexp.learnflowapi.admin.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.admin.model.Approval;
import com.teamexp.learnflowapi.admin.model.ApprovalRejectType;
import com.teamexp.learnflowapi.admin.repository.ApprovalRepository;
import com.teamexp.learnflowapi.ai.model.AiTask;
import com.teamexp.learnflowapi.ai.repository.AiTaskRepository;
import com.teamexp.learnflowapi.lecture.model.Chapter;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.repository.LectureAdminRepository;
import com.teamexp.learnflowapi.lecture.service.LectureAdminService;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ApprovalService {

    @Value("${spring.application.default-thumbnail}")
    private String defaultThumbnailUrl;

    private final UserRepository userRepository;
    private final LectureAdminRepository lectureAdminRepository;
    private final LectureAdminService lectureAdminService;
    private final ApprovalRepository approvalRepository;
    private final AiTaskRepository aiTaskRepository;

    public ApprovalService(
        UserRepository userRepository,
        LectureAdminRepository lectureAdminRepository,
        ApprovalRepository approvalRepository,
        LectureAdminService lectureAdminService,
        AiTaskRepository aiTaskRepository) {

        this.userRepository = userRepository;
        this.lectureAdminRepository = lectureAdminRepository;
        this.approvalRepository = approvalRepository;
        this.lectureAdminService = lectureAdminService;
        this.aiTaskRepository = aiTaskRepository;
    }

    public ApprovalsResponse getApprovals(Pageable pageable) {
        Page<Lecture> lectures = lectureAdminRepository.findAllSubmittedLectures(pageable);

        List<String> instructorIds = lectures.getContent().stream()
            .map(Lecture::getInstructorId)
            .filter(id -> id != null && !id.isBlank())
            .distinct()
            .toList();

        Map<String, String> nicknameMap = userRepository.findAllById(instructorIds).stream()
            .collect(Collectors.toMap(
                User::getUserId,
                user -> user.getNickname() != null ? user.getNickname() : "알 수 없음",
                (existing, replacement) -> existing
            ));

        List<ApprovalDto> approvals = lectures.getContent().stream()
            .map(lecture -> {
                String instructorName = nicknameMap.getOrDefault(lecture.getInstructorId(), "알 수 없음");
                String finalThumbnailUrl = (lecture.getThumbnailUrl() != null && !lecture.getThumbnailUrl().isBlank())
                    ? lecture.getThumbnailUrl()
                    : defaultThumbnailUrl;

                return new ApprovalDto(
                    lecture.getId(),
                    finalThumbnailUrl,
                    lecture.getTitle(),
                    instructorName,
                    lecture.getUpdatedAt(),
                    lecture.getStatus().name()
                );
            })
            .toList();

        return new ApprovalsResponse(
            lectures.getTotalElements(),
            lectures.getNumber(),
            lectures.getSize(),
            approvals
        );
    }

    @Transactional
    public void createApproval(Long lectureId) {
        Approval newApproval = Approval.create(lectureId);
        approvalRepository.save(newApproval);
    }

    @Transactional
    public void createApproval(Long lectureId, List<ApprovalRejectType> rejectedReasons, String reason) {
        Approval newApproval = Approval.create(lectureId, rejectedReasons, reason);
        approvalRepository.save(newApproval);
    }

    public ApprovalDetailResponse getApproval(Long lectureId) {
        Lecture foundLecture = lectureAdminRepository.findByIdWithChaptersAndLessonsAndQuizzes(lectureId).orElseThrow(
            LectureNotFoundException::new
        );

        User foundUser = userRepository.findByUserIdAndDelFlagFalse(foundLecture.getInstructorId()).orElse(null);

        return ApprovalDetailResponse.of(foundLecture, foundUser);
    }

    @Transactional
    public ApprovalUpdateResponse updateApproval(Long lectureId, ApprovalUpdateRequest request) {
        // N+1 방지: Fetch Join 사용
        Lecture foundedLecture = lectureAdminRepository.findByIdWithChaptersAndLessonsAndQuizzes(lectureId).orElseThrow(
            LectureNotFoundException::new
        );

        ApprovalStatus approvalStatus = request.status();
        Approval approval = Approval.create(
            lectureId,
            request.rejectCategories(),
            request.reason()
        );
        approvalRepository.saveAndFlush(approval);

        String status = switch (approvalStatus) {
            case APPROVED -> {
                lectureAdminService.allowPublishLecture(lectureId);

                // AI 작업 트리거: 중복 생성 방지 로직 추가
                List<Lesson> videoLessons = foundedLecture.getChapters().stream()
                    .flatMap(chapter -> chapter.getLessons().stream())
                    .toList(); // Lesson 필터링 조건(isVideoType)이 있다면 추가

                if (!videoLessons.isEmpty()) {
                    List<Long> lessonIds = videoLessons.stream().map(Lesson::getId).toList();
                    // 이미 존재하는 Task 조회
                    List<Long> existingTaskLessonIds = aiTaskRepository.findAllLessonIdsByLessonIdIn(lessonIds);

                    List<AiTask> newTasks = videoLessons.stream()
                        .filter(lesson -> !existingTaskLessonIds.contains(lesson.getId()))
                        .map(lesson -> AiTask.create(lesson.getId()))
                        .toList();

                    if (!newTasks.isEmpty()) {
                        aiTaskRepository.saveAll(newTasks);
                    }
                }
                yield "PUBLISHED";
            }
            case REJECTED -> {
                lectureAdminService.notAllowPublishLecture(lectureId);
                yield "REJECTED";
            }
        };

        return new ApprovalUpdateResponse(
            lectureId,
            status,
            approval.getUpdatedAt()
        );
    }
}

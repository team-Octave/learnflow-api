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
import com.teamexp.learnflowapi.ai.model.AiTask; // ✨ 추가
import com.teamexp.learnflowapi.ai.repository.AiTaskRepository; // ✨ 추가
import com.teamexp.learnflowapi.lecture.model.Chapter; // ✨ 추가
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.Lesson; // ✨ 추가
import com.teamexp.learnflowapi.lecture.repository.LectureAdminRepository;
import com.teamexp.learnflowapi.lecture.service.LectureAdminService;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList; // ✨ 추가
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
    private final AiTaskRepository aiTaskRepository; // ✨ [추가] AI 작업 저장소 주입

    public ApprovalService(
        UserRepository userRepository,
        LectureAdminRepository lectureAdminRepository,
        ApprovalRepository approvalRepository,
        LectureAdminService lectureAdminService,
        AiTaskRepository aiTaskRepository) { // ✨ 생성자 파라미터 추가

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

    // ✨ [수정] 강의 승인 시 AI 작업 트리거 로직 추가
    @Transactional
    public ApprovalUpdateResponse updateApproval(Long lectureId, ApprovalUpdateRequest request) {

        // Lecture 조회 (챕터/레슨 정보가 필요하므로 Fetch Join된 메서드 사용 권장, 없으면 일반 조회 후 Lazy Loading)
        // 여기서는 안전하게 챕터/레슨을 가져오기 위해 기존에 있는 findByIdWith...를 쓰는게 좋지만,
        // 일단 기존 코드 흐름(findById)을 유지하면서 설명하겠습니다.
        // *주의: Lazy Loading이 발생할 수 있으므로 @Transactional 필수
        Lecture foundedLecture = lectureAdminRepository.findById(lectureId).orElseThrow(
            LectureNotFoundException::new
        );

        // approval 데이터 저장
        ApprovalStatus approvalStatus = request.status();
        Approval approval = Approval.create(
            lectureId,
            request.rejectCategories(),
            request.reason()
        );
        approvalRepository.saveAndFlush(approval);

        // Lecture의 상태 값 업데이트 & AI 트리거
        String status = switch (approvalStatus) {
            case APPROVED -> {
                lectureAdminService.allowPublishLecture(lectureId);

                // ✨ [AI Trigger] 강의 승인 시 요약 작업 예약 (Outbox Pattern)
                List<AiTask> tasks = new ArrayList<>();

                // 강의 하위의 모든 챕터 -> 모든 레슨 순회
                for (Chapter chapter : foundedLecture.getChapters()) {
                    for (Lesson lesson : chapter.getLessons()) {
                        // 각 레슨마다 AI 요약 작업(READY) 생성
                        tasks.add(AiTask.create(lesson.getId()));
                    }
                }

                if (!tasks.isEmpty()) {
                    aiTaskRepository.saveAll(tasks); // 일괄 저장
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

package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.dto.ApprovalDetailResponse;
import com.teamexp.learnflowapi.admin.dto.ApprovalDto;
import com.teamexp.learnflowapi.admin.dto.ApprovalUpdateResponse;
import com.teamexp.learnflowapi.admin.dto.ApprovalsResponse;
import com.teamexp.learnflowapi.admin.dto.request.ApprovalStatus;
import com.teamexp.learnflowapi.admin.dto.request.ApprovalUpdateRequest;
import com.teamexp.learnflowapi.admin.exception.ApprovalNotFoundException;
import com.teamexp.learnflowapi.admin.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.admin.model.Approval;
import com.teamexp.learnflowapi.admin.model.ApprovalRejectReason;
import com.teamexp.learnflowapi.admin.repository.ApprovalRepository;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.repository.LectureAdminRepository;
import com.teamexp.learnflowapi.lecture.service.LectureAdminService;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.repository.UserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ApprovalService {


    @Value("${spring.application.default-thumbnail}")
    private String defaultThumbnailUrl;

    // TODO : 여기서 approvals 테이블을 관리하는 repository를 사용하는게 맞는것 같음.

    private final UserRepository userRepository;
    private final LectureAdminRepository  lectureAdminRepository;
    private final LectureAdminService lectureAdminService;
    private final ApprovalRepository approvalRepository;

    public ApprovalService(
        UserRepository userRepository,
        LectureAdminRepository lectureAdminRepository,
        ApprovalRepository approvalRepository,
        LectureAdminService lectureAdminService) {

        this.userRepository = userRepository;
        this.lectureAdminRepository = lectureAdminRepository;
        this.approvalRepository = approvalRepository;
        this.lectureAdminService = lectureAdminService;
    }

    public ApprovalsResponse getApprovals(Pageable pageable) {

        // TODO : 추후 LectureAdminRepository로 변경
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
        // DTO Mapping
        List<ApprovalDto> approvals = lectures.getContent().stream()
            .map(lecture -> {
                String instructorName = "알 수 없음";
                if (lecture.getInstructorId() != null) {
                    instructorName = nicknameMap.getOrDefault(lecture.getInstructorId(), "알 수 없음");
                }

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

    /*
    * lecture가 존재한다고 가정하에 매개변수로 받는다.
    * */
    @Transactional
    public void createApproval(Long lectureId) {


        // Approval 객체 생성
        Approval newApproval = Approval.create(lectureId);

        // DB에 저장
        approvalRepository.save(newApproval);

    }

    /*
    * create approval overload
    * TODO : 해당 메서드는 추후에 필요 없을 것 같음.
    * */
    @Transactional
    public void createApproval(Long lectureId, List<ApprovalRejectReason> rejectedReasons, String reason) {


        // Approval 객체 생성
        Approval newApproval = Approval.create(lectureId);

        // DB에 저장
        approvalRepository.save(newApproval);

    }

    // TODO : 현재는 LectureId이지만 추후에 ApprovalId로 변경해야 함.
    public ApprovalDetailResponse getApproval(Long lectureId) {

//        // Approval을 찾을 수 없음
//        Approval foundApproval = approvalRepository.findById(approvalId).orElseThrow(
//            ApprovalNotFoundException::new
//        );
//
//        // lecture 정보 추출
//        Long lectureId = foundApproval.getLectureId();
        Lecture foundLecture = lectureAdminRepository.findByIdWithChaptersAndLessonsAndQuizzes(lectureId).orElseThrow(
            LectureNotFoundException::new
        );

        // 강의 생성자 이름 추출
        User foundUser = userRepository.findByUserIdAndDelFlagFalse(foundLecture.getInstructorId()).orElse(null);

        // DTO 맵핑
        return ApprovalDetailResponse.of(foundLecture, foundUser);
    }

    // TODO : 현재는 LectureId이지만 추후에 ApprovalId로 변경해야함.
    @Transactional
    public ApprovalUpdateResponse updateApproval(Long lectureId, ApprovalUpdateRequest request) {

        // Lecture 조회 - Exception : 찾을 수 없는 경우 발생
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

        // Lecture의 상태 값 업데이트
        String status = switch (approvalStatus) {
            case APPROVED -> {
                lectureAdminService.allowPublishLecture(lectureId);
                yield "PUBLISHED";
            }
            case REJECTED -> {
                lectureAdminService.notAllowPublishLecture(lectureId);
                yield "REJECTED";
            }
        };


        // ApprovalUpdateResponse DTO Mapping
        return new ApprovalUpdateResponse(
            lectureId,
            status,
            approval.getUpdatedAt()
        );


    }

}

package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.dto.ApprovalDto;
import com.teamexp.learnflowapi.admin.dto.ApprovalsResponse;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import com.teamexp.learnflowapi.user.model.User;
import com.teamexp.learnflowapi.user.repository.UserRepository;
import com.teamexp.learnflowapi.user.service.UserService;
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
    private final LectureRepository lectureRepository;
    private final UserRepository userRepository;

    public ApprovalService(LectureRepository lectureRepository, UserRepository userRepository) {
        this.lectureRepository = lectureRepository;
        this.userRepository = userRepository;
    }

    public ApprovalsResponse getApprovals(Pageable pageable) {

        // TODO : 추후 LectureAdminRepository로 변경
        Page<Lecture> lectures = lectureRepository.findByStatus(LectureStatus.SUBMITTED, pageable);


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

}

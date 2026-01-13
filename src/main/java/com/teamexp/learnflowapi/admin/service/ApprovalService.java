package com.teamexp.learnflowapi.admin.service;

import com.teamexp.learnflowapi.admin.controller.dto.ApprovalDto;
import com.teamexp.learnflowapi.admin.controller.dto.ApprovalsResponse;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureStatus;
import com.teamexp.learnflowapi.lecture.repository.LectureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ApprovalService {

    // TODO : 여기서 approvals 테이블을 관리하는 repository를 사용하는게 맞는것 같음.
    private final LectureRepository lectureRepository;

    public ApprovalService(LectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
    }

    public ApprovalsResponse getApprovals(Pageable pageable) {
        // TODO : 이부분에 Submit으로 변경해야함.
        Page<Lecture> lectures = lectureRepository.findByStatus(LectureStatus.UNAVAILABLE, pageable);

        // DTO Mapping
        List<ApprovalDto> approvals = lectures.getContent().stream()
            .map(lecture -> new ApprovalDto(
                1L,                    // TODO : 이 부분을 맵핑 해줘야 함.
                lecture.getId(),                   // lectureId
                "test.com",                        // TODO : 썸네일 가져와야함.
                lecture.getTitle(),
                "sayhoon",               // TODO : 강사 이름 가져와야함.
                lecture.getCreatedAt(),           // TODO : Updated at으로 수정해야 함.
                lecture.getStatus().name()
            ))
            .toList();

        return new ApprovalsResponse(
            (int) lectures.getTotalElements(),
            lectures.getNumber(),
            lectures.getSize(),
            approvals
        );

    }

}

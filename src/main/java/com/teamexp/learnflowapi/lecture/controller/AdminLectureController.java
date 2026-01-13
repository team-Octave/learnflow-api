package com.teamexp.learnflowapi.lecture.controller;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.lecture.dto.response.AdminLectureResponse;
import com.teamexp.learnflowapi.lecture.model.Lecture;
import com.teamexp.learnflowapi.lecture.model.LectureSortType;
import com.teamexp.learnflowapi.lecture.exception.LectureNotFoundException;
import com.teamexp.learnflowapi.lecture.repository.JpaLectureRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/lectures")
public class AdminLectureController {

    private final JpaLectureRepository lectureRepository;

    public AdminLectureController(JpaLectureRepository lectureRepository) {
        this.lectureRepository = lectureRepository;
    }

    // Admin can view deleted lectures (default includeDeleted=true)
    @GetMapping
    public ResponseEntity<BaseResponse<Page<AdminLectureResponse>>> list(
        @RequestParam(required = false, defaultValue = "LATEST") String sort,
        @PageableDefault(size = 16) Pageable pageable
    ) {
        String sortBy = sort != null && !sort.isEmpty() ? sort : "LATEST";
        try {
            LectureSortType.forEntity(sortBy);
        } catch (Exception e) {
            sortBy = "LATEST";
        }

        Pageable pageableWithoutSort = PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
        Page<Lecture> lecturePage = lectureRepository.findAllWithStatsForAdmin(sortBy, pageableWithoutSort);

        Page<AdminLectureResponse> response = lecturePage.map(AdminLectureResponse::from);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(response));
    }

    @GetMapping("/{lectureId}")
    public ResponseEntity<BaseResponse<AdminLectureResponse>> get(@PathVariable Long lectureId) {
        // Minimal admin view (includes deleteFlag/deletedAt). Detail endpoint can be expanded later.
        Lecture lecture = lectureRepository.findByIdWithStatisticForAdmin(lectureId)
            .orElseThrow(LectureNotFoundException::new);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(AdminLectureResponse.from(lecture)));
    }
}



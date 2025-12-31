package com.teamexp.learnflowapi.lecture.controller;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import com.teamexp.learnflowapi.lecture.dto.request.LectureCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureFullCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.response.LectureFullCreateResponse;
import com.teamexp.learnflowapi.lecture.dto.response.LectureResponse;
import com.teamexp.learnflowapi.lecture.dto.response.PublishedResponse;
import com.teamexp.learnflowapi.lecture.service.LectureService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1/lectures")
public class LectureController {

    private final LectureService lectureService;

    public LectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @Deprecated // V2로 대체 예정
    @PostMapping
    public ResponseEntity<BaseResponse<LectureResponse>> createLecture(
        @Valid @RequestBody LectureCreateRequest lectureCreateRequest,
        @AuthenticationPrincipal CustomUserPrincipal customUser
        ) {
        LectureResponse lectureResponse = lectureService.createLecture(lectureCreateRequest, customUser.getId(), customUser.getNickname());

        return  ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(lectureResponse));
    }

    @Deprecated // addChapter + addLesson + BindCurriculum 등으로 V2에서 대체 예정
    @PostMapping("/{lectureId}/curriculum")
    public ResponseEntity<BaseResponse<LectureFullCreateResponse>> createLectureFullCurriculum(
        @PathVariable Long lectureId,
        @Valid @RequestBody LectureFullCreateRequest request,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        LectureFullCreateResponse response = lectureService.createLectureFullCurriculum(
            lectureId,
            request,
            customUser.getId(),
            customUser.getNickname()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(response));
    }


    // 강의 출판 (강의 상태를 DRAFT -> PUBLISHED로 변경) & 멱등성 보장을 위해 PUT 메서드 사용
    @PutMapping("/{lectureId}/publish")
    public ResponseEntity<BaseResponse<PublishedResponse>> publishLecture(
        @PathVariable Long lectureId,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        PublishedResponse publishedResponse = lectureService.makeAvailableLecture(lectureId, customUser.getId());

        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(publishedResponse));
    }

    // 내 강의 조회 
    // 강사의 강의 목록 조회 order by updatedAt 최신 순으로 하려면, Pageable에 정렬 정보 추가 필요
    @GetMapping("/my")
    public ResponseEntity<BaseResponse<Page<LectureResponse>>> getMyLectures(
        @AuthenticationPrincipal CustomUserPrincipal customUser,
        @PageableDefault(size = 16, sort = "updatedAt,desc") Pageable pageable
    ) {
        Page<LectureResponse> lectures = lectureService.getLecturesByInstructor(customUser.getId(), pageable);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(lectures));
    }

    // 강의 목록 조회(?category=2&level=BEGINNER&sort=POPULAR)
    @GetMapping
    public ResponseEntity<BaseResponse<Page<LectureResponse>>> getAllLectures(
        @RequestParam(required = false, defaultValue = "ALL") String category, // case "ALL" means no filter, if not "ALL", then filter by categoryId as String 
        @RequestParam(required = false, defaultValue = "ALL") String level, // case "ALL" means no filter, if not "ALL", then filter by level
        @RequestParam(required = false, defaultValue = "POPULAR") String sort, // POPULAR, RATING, LATEST
        @PageableDefault(size = 16) Pageable pageable
    ) {
        Page<LectureResponse> lectures = lectureService.getAllLecturesWithFilters(category, level, sort, pageable);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(lectures));
    }

    // 강의 단건 조회 - user view & instructor view
    @GetMapping("/{lectureId}")
    public ResponseEntity<BaseResponse<LectureResponse>> getLecture(
        @PathVariable Long lectureId
    ) {
        LectureResponse lecture = lectureService.getLecture(lectureId);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(lecture));
    }

    // 강의 삭제
    @DeleteMapping("/{lectureId}" )
    public ResponseEntity<BaseResponse<Void>> deleteLecture(
        @PathVariable("lectureId") Long lectureId,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        lectureService.deleteLecture(lectureId, customUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(null));
    }

}

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

    @PostMapping
    public ResponseEntity<BaseResponse<LectureResponse>> createLecture(
        @Valid @RequestBody LectureCreateRequest lectureCreateRequest,
        @AuthenticationPrincipal CustomUserPrincipal customUser
        ) {
        LectureResponse lectureResponse = lectureService.createLecture(lectureCreateRequest, customUser.getId(), customUser.getNickname());

        return  ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(lectureResponse));
    }

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


//    // request : Chapter / response : LectureResponse
//    @PostMapping("/{lectureId}/chapters")
//    public ResponseEntity<LectureResponse> addChapter(
//        @Valid @RequestBody ChapterCreateRequest chapterCreateRequest,
//        @PathVariable Long lectureId,
//        @AuthenticationPrincipal CustomUserPrincipal customUser
//        ) {
//        LectureResponse lectureResponse = lectureService.addChapter(chapterCreateRequest, lectureId, customUser.getId());
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(lectureResponse);
//    }
//
//    // request : Lesson / response : LectureResponse
//    // Lesson 생성 시, ChapterId를 queryParam로 받아야할지? body로 받아야할지?
//    @PostMapping("/{lectureId}/chapters/{chapterId}/lessons")
//    public ResponseEntity<LectureResponse> addLesson(
//        @Valid @RequestBody LessonCreateRequest lessonCreateRequest,
//        @PathVariable Long lectureId,
//        @PathVariable Long chapterId,
//        @AuthenticationPrincipal CustomUserPrincipal customUser
//        ) {
//        LectureResponse lectureResponse = lectureService.addLesson(lessonCreateRequest, lectureId, chapterId, customUser.getId());
//
//        return ResponseEntity.status(HttpStatus.CREATED).body(lectureResponse);
//    }

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
    @GetMapping("/my")
    public ResponseEntity<BaseResponse<Page<LectureResponse>>> getMyLectures(
        @AuthenticationPrincipal CustomUserPrincipal customUser,
        @PageableDefault(size = 16) Pageable pageable
    ) {
        Page<LectureResponse> lectures = lectureService.getLecturesByInstructor(customUser.getId(), pageable);
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(lectures));
    }

    // 강의 목록 조회
    @GetMapping
    public ResponseEntity<BaseResponse<Page<LectureResponse>>> getAllLectures(
        @RequestParam(required = false, defaultValue = "ALL") String category,
        @RequestParam(required = false, defaultValue = "ALL") String level,
        @RequestParam(required = false, defaultValue = "POPULAR") String sort,
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

}

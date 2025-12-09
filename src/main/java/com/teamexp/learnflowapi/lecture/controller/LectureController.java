package com.teamexp.learnflowapi.lecture.controller;

import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import com.teamexp.learnflowapi.lecture.dto.request.ChapterCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureFullCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LessonCreateRequest;
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
import org.springframework.web.bind.annotation.RestController;
import org.springframework.http.ResponseEntity;

import java.util.List;

@RestController
@RequestMapping("/api/v1/lectures")
public class LectureController {

    private final LectureService lectureService;

    LectureController(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @PostMapping
    public ResponseEntity<LectureResponse> createLecture(
        @Valid @RequestBody LectureCreateRequest lectureCreateRequest,
        @AuthenticationPrincipal CustomUserPrincipal customUser
        ) {
        LectureResponse lectureResponse = lectureService.createLecture(lectureCreateRequest, customUser.getId());

        return  ResponseEntity.status(HttpStatus.CREATED).body(lectureResponse);
    }

    @PostMapping("/{lectureId}/curriculum")
    public ResponseEntity<LectureFullCreateResponse> createLectureFullCurriculum(
        @PathVariable Long lectureId,
        @Valid @RequestBody LectureFullCreateRequest request,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        LectureFullCreateResponse response = lectureService.createLectureFullCurriculum(
            lectureId,
            request,
            customUser.getId()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
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
    public ResponseEntity<PublishedResponse> publishLecture(
        @PathVariable Long lectureId,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        PublishedResponse publishedResponse = lectureService.makeAvailableLecture(lectureId, customUser.getId());

        return ResponseEntity.status(HttpStatus.OK).body(publishedResponse);
    }

    // 내 강의 조회
    @GetMapping("/my")
    public ResponseEntity<List<LectureResponse>> getMyLectures(
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        List<LectureResponse> lectures = lectureService.getLecturesByInstructor(customUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(lectures);
    }

    // 강의 목록 조회
    @GetMapping
    public ResponseEntity<List<LectureResponse>> getAllLectures(

    ) {
        List<LectureResponse> lectures = lectureService.getAllLectures();
        return ResponseEntity.status(HttpStatus.OK).body(lectures);
    }

    // 강의 단건 조회 - user view & instructor view
    @GetMapping("/{lectureId}")
    public ResponseEntity<LectureResponse> getLecture(
        @PathVariable Long lectureId
    ) {
        LectureResponse lecture = lectureService.getLecture(lectureId);
        return ResponseEntity.status(HttpStatus.OK).body(lecture);
    }

}

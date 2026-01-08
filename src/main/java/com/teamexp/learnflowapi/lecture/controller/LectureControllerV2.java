package com.teamexp.learnflowapi.lecture.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PathVariable;

import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.global.security.principal.CustomUserPrincipal;
import com.teamexp.learnflowapi.lecture.dto.request.ChapterCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.ChapterUpdateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.CurriculumBindRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LectureCreateRequestV2;
import com.teamexp.learnflowapi.lecture.dto.request.LessonCreateRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LessonQuizReplaceRequest;
import com.teamexp.learnflowapi.lecture.dto.request.LessonUpdateRequest;
import com.teamexp.learnflowapi.lecture.dto.response.ChapterResponse;
import com.teamexp.learnflowapi.lecture.dto.response.LectureResponse;
import com.teamexp.learnflowapi.lecture.dto.response.LessonResponse;
import com.teamexp.learnflowapi.lecture.service.LectureService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v2/lectures")
public class LectureControllerV2 {

    private final LectureService lectureService;

    public LectureControllerV2(LectureService lectureService) {
        this.lectureService = lectureService;
    }

    @PostMapping
    public ResponseEntity<BaseResponse<LectureResponse>> createLecture(
        @Valid @RequestBody LectureCreateRequestV2 lectureCreateRequest,
        @AuthenticationPrincipal CustomUserPrincipal customUser
        ) {
        LectureResponse lectureResponse = lectureService.createLecture(lectureCreateRequest, customUser.getId(), customUser.getNickname());

        return  ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(lectureResponse));
    }

    // ===== Curriculum V2 (incremental) =====

    @PostMapping("/{lectureId}/chapters")
    public ResponseEntity<BaseResponse<ChapterResponse>> addChapter(
        @PathVariable Long lectureId,
        @Valid @RequestBody ChapterCreateRequest request,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        ChapterResponse response = lectureService.addChapterV2(lectureId, request, customUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(response));
    }

    @PatchMapping("/{lectureId}/chapters/{chapterId}")
    public ResponseEntity<BaseResponse<ChapterResponse>> updateChapter(
        @PathVariable Long lectureId,
        @PathVariable Long chapterId,
        @Valid @RequestBody ChapterUpdateRequest request,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        ChapterResponse response = lectureService.updateChapterV2(lectureId, chapterId, request, customUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(response));
    }

    @DeleteMapping("/{lectureId}/chapters/{chapterId}")
    public ResponseEntity<BaseResponse<Void>> deleteChapter(
        @PathVariable Long lectureId,
        @PathVariable Long chapterId,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        lectureService.deleteChapterV2(lectureId, chapterId, customUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(null));
    }

    @PostMapping("/{lectureId}/chapters/{chapterId}/lessons")
    public ResponseEntity<BaseResponse<LessonResponse>> addLesson(
        @PathVariable Long lectureId,
        @PathVariable Long chapterId,
        @Valid @RequestBody LessonCreateRequest request,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        LessonResponse response = lectureService.addLessonV2(lectureId, chapterId, request, customUser.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(BaseResponse.ok(response));
    }

    @PatchMapping("/{lectureId}/lessons/{lessonId}")
    public ResponseEntity<BaseResponse<LessonResponse>> updateLesson(
        @PathVariable Long lectureId,
        @PathVariable Long lessonId,
        @RequestBody LessonUpdateRequest request,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        LessonResponse response = lectureService.updateLessonV2(lectureId, lessonId, request, customUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(response));
    }

    @DeleteMapping("/{lectureId}/lessons/{lessonId}")
    public ResponseEntity<BaseResponse<Void>> deleteLesson(
        @PathVariable Long lectureId,
        @PathVariable Long lessonId,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        lectureService.deleteLessonV2(lectureId, lessonId, customUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(null));
    }

    // @PutMapping("/{lectureId}/lessons/{lessonId}/quiz")
    // public ResponseEntity<BaseResponse<LessonResponse>> replaceLessonQuiz(
    //     @PathVariable Long lectureId,
    //     @PathVariable Long lessonId,
    //     @Valid @RequestBody LessonQuizReplaceRequest request,
    //     @AuthenticationPrincipal CustomUserPrincipal customUser
    // ) {
    //     LessonResponse response = lectureService.replaceLessonQuizV2(lectureId, lessonId, request, customUser.getId());
    //     return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(response));
    // }

    @PutMapping("/{lectureId}/curriculum/bind")
    public ResponseEntity<BaseResponse<Void>> bindCurriculum(
        @PathVariable Long lectureId,
        @Valid @RequestBody CurriculumBindRequest request,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        lectureService.bindAndReorderCurriculumV2(lectureId, request, customUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(null));
    }

    // Get Lesson Response without Quiz Questions
    @GetMapping("/{lectureId}/lessons/{lessonId}")
    public ResponseEntity<BaseResponse<LessonResponse>> getLesson(
        @PathVariable Long lectureId,
        @PathVariable Long lessonId,
        @AuthenticationPrincipal CustomUserPrincipal customUser
    ) {
        LessonResponse response = lectureService.getLessonV2(lectureId, lessonId, customUser.getId());
        return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(response));
    }

    // Get Lesson Response with Quiz Questions
    // @GetMapping("/{lectureId}/lessons/{lessonId}/quiz")
    // public ResponseEntity<BaseResponse<LessonResponse>> getLessonWithQuiz(
    //     @PathVariable Long lectureId,
    //     @PathVariable Long lessonId,
    //     @AuthenticationPrincipal CustomUserPrincipal customUser
    // ) {
    //     LessonResponse response = lectureService.getLessonWithQuizV2(lectureId, lessonId, customUser.getId());
    //     return ResponseEntity.status(HttpStatus.OK).body(BaseResponse.ok(response));
    // }
}

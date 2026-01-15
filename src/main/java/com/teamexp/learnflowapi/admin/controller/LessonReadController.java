package com.teamexp.learnflowapi.admin.controller;

import com.teamexp.learnflowapi.admin.dto.LessonReadDetailResponse;
import com.teamexp.learnflowapi.admin.service.LessonReadService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("api/v1/admin/lectures")
public class LessonReadController {

    private final LessonReadService lessonReadService;

    public LessonReadController(LessonReadService lessonReadService) {
        this.lessonReadService = lessonReadService;
    }

    @GetMapping("/{lectureId}/lessons/{lessonId}")
    public ResponseEntity<BaseResponse<LessonReadDetailResponse>> getLesson(
        @PathVariable(name = "lectureId") Long lectureId,
        @PathVariable(name = "lessonId") Long lessonId
    ) {
        LessonReadDetailResponse response = lessonReadService.readLesson(lectureId, lessonId);
        return ResponseEntity
            .status(HttpStatus.OK)
            .body(BaseResponse.ok(response));
    }
}

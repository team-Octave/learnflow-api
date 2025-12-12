package com.teamexp.learnflowapi.content.controller;

import com.teamexp.learnflowapi.content.dto.QuizRequest;
import com.teamexp.learnflowapi.content.dto.QuizResponse;
import com.teamexp.learnflowapi.content.dto.QuizSaveRequest;

import com.teamexp.learnflowapi.content.service.QuizService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("api/v1/contents/")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // 레슨 퀴즈 조회
    @GetMapping("lessons/{lessonId}/quizzes")
    public ResponseEntity<BaseResponse<List<QuizResponse>>> getQuizzesByLesson(
            @PathVariable Long lessonId
    ) {
        List<QuizResponse> response = quizService.getQuizzesByLesson(lessonId);
        return ResponseEntity.ok(BaseResponse.ok(response));
    }

    // 퀴즈 저장
    @PostMapping("lessons/{lessonId}/quizzes")
    public ResponseEntity<BaseResponse<List<QuizResponse>>> createQuizzes(
            @PathVariable Long lessonId,
            @Valid @RequestBody QuizSaveRequest request
    ) {
        List<QuizResponse> response = quizService.createQuiz(lessonId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(BaseResponse.ok(response));
    }

}


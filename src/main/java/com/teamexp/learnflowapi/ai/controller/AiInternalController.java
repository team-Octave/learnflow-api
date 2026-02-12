package com.teamexp.learnflowapi.ai.controller;

import com.teamexp.learnflowapi.ai.dto.AiJobResponse;
import com.teamexp.learnflowapi.ai.dto.AiJobResultRequest;
import com.teamexp.learnflowapi.ai.service.AiWorkService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * @deprecated AiTaskController로 대체됨.
 * 신규 Worker는 /api/internal/ai/tasks/* 엔드포인트 사용 권장.
 */
@Deprecated
@Slf4j
@RestController
@RequestMapping("/api/internal/ai")
@RequiredArgsConstructor
@Validated
public class AiInternalController {

    private final AiWorkService aiWorkService;

    @GetMapping("/tasks")
    public BaseResponse<List<AiJobResponse>> getPendingTasks(
        @RequestParam(defaultValue = "10") @Min(1) @Max(50) int limit // 상한/하한 제한
    ) {
        List<AiJobResponse> tasks = aiWorkService.fetchPendingTasks(limit);
        return new BaseResponse<>(tasks);
    }

    @PostMapping("/result")
    public BaseResponse<String> submitResult(@RequestBody AiJobResultRequest request) {
        aiWorkService.processResult(request);
        return new BaseResponse<>("Result processed successfully");
    }
}

package com.teamexp.learnflowapi.ai.controller;

import com.teamexp.learnflowapi.ai.dto.AiJobResponse;
import com.teamexp.learnflowapi.ai.dto.AiJobResultRequest;
import com.teamexp.learnflowapi.ai.service.AiWorkService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/internal/ai")
@RequiredArgsConstructor
public class AiInternalController {

    private final AiWorkService aiWorkService;

    @GetMapping("/tasks")
    public BaseResponse<List<AiJobResponse>> getPendingTasks(@RequestParam(defaultValue = "1") int limit) {
        List<AiJobResponse> tasks = aiWorkService.fetchPendingTasks(limit);
        return new BaseResponse<>(tasks);
    }

    @PostMapping("/result")
    public BaseResponse<String> submitResult(@RequestBody AiJobResultRequest request) {
        aiWorkService.processResult(request);
        return new BaseResponse<>("Result processed successfully");
    }
}

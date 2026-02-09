package com.teamexp.learnflowapi.ai.controller;

import com.teamexp.learnflowapi.ai.dto.AiSummaryApiResponse;
import com.teamexp.learnflowapi.ai.service.AiSummaryService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiSummaryController {

    private final AiSummaryService aiSummaryService;

    @GetMapping("/summary/{lessonId}")
    public BaseResponse<AiSummaryApiResponse> getSummary(@PathVariable Long lessonId) {
        AiSummaryApiResponse response = aiSummaryService.getSummary(lessonId);

        // 상태가 PROCESSING이어도 API 호출 자체는 성공이므로 200 OK + BaseResponse
        return new BaseResponse<>(response);
    }
}

package com.teamexp.learnflowapi.ai.controller;

import com.teamexp.learnflowapi.ai.dto.*;
import com.teamexp.learnflowapi.ai.service.AiTaskService;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.context.request.async.DeferredResult;

/**
 * AI 태스크 관리 API (Long Polling)
 *
 * 인증: X-Internal-Api-Key 헤더 (InternalApiKeyFilter에서 처리)
 */
@Slf4j
@RestController
@RequestMapping("/api/internal/ai/tasks")
@RequiredArgsConstructor
@Validated
public class AiTaskController {

    private final AiTaskService aiTaskService;

    /**
     * Long polling으로 처리할 태스크 조회
     *
     * @param workerId 워커 식별자
     * @param timeout 폴링 타임아웃 (초)
     * @return 태스크 정보 또는 빈 응답
     */
    @GetMapping("/poll")
    public DeferredResult<BaseResponse<AiTaskPollResponse>> pollTask(
        @RequestParam("worker_id") @NotBlank String workerId,
        @RequestParam(value = "timeout", defaultValue = "30") @Min(1) @Max(60) int timeout
    ) {
        DeferredResult<BaseResponse<AiTaskPollResponse>> result =
            new DeferredResult<>(timeout * 1000L + 5000L, // 약간의 여유 시간
                () -> new BaseResponse<>(AiTaskPollResponse.empty()));

        aiTaskService.pollTaskAsync(workerId, timeout, result);
        return result;
    }

    /**
     * 태스크 완료 보고
     *
     * @param taskId 태스크 ID
     * @param request 완료 정보 (transcript, fullAnalysis, summary 등)
     * @return 처리 결과
     */
    @PostMapping("/{taskId}/complete")
    public BaseResponse<AiTaskCompleteResponse> completeTask(
        @PathVariable Long taskId,
        @RequestBody @Valid AiTaskCompleteRequest request
    ) {
        AiTaskCompleteResponse response = aiTaskService.completeTask(taskId, request);
        return new BaseResponse<>(response);
    }

    /**
     * 태스크 실패 보고
     *
     * @param taskId 태스크 ID
     * @param request 실패 정보 (errorCode, errorMessage, isRetryable)
     * @return 재시도 정보
     */
    @PostMapping("/{taskId}/fail")
    public BaseResponse<AiTaskFailResponse> failTask(
        @PathVariable Long taskId,
        @RequestBody @Valid AiTaskFailRequest request
    ) {
        AiTaskFailResponse response = aiTaskService.failTask(taskId, request);
        return new BaseResponse<>(response);
    }

    /**
     * 하트비트 전송 (진행 상황 업데이트)
     *
     * @param taskId 태스크 ID
     * @param request 진행 정보 (progress, currentStep)
     * @return 계속 처리 여부 (shouldContinue)
     */
    @PostMapping("/{taskId}/heartbeat")
    public BaseResponse<AiTaskHeartbeatResponse> heartbeat(
        @PathVariable Long taskId,
        @RequestBody @Valid AiTaskHeartbeatRequest request
    ) {
        AiTaskHeartbeatResponse response = aiTaskService.heartbeat(taskId, request);
        return new BaseResponse<>(response);
    }
}

package com.teamexp.learnflowapi.ai.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AiJobResultRequest(
    Long taskId,
    boolean success,
    @JsonProperty("summary") String summaryJson, // 성공 시 요약 내용 (JSON String)
    String errorMessage // 실패 시 에러 메시지
) {}

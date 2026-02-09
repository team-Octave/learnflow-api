package com.teamexp.learnflowapi.ai.dto;

import com.teamexp.learnflowapi.ai.model.AiSummaryContent;

public record AiSummaryApiResponse(
    Long lessonId,
    AiSummaryContent content, // Step 1에서 만든 JSON 객체 (overview, keyTakeaways)
    String status             // "COMPLETED", "PROCESSING", "READY", "FAILED", "NOT_FOUND"
) {
    // 팩토리 메서드: 요약 완료됨
    public static AiSummaryApiResponse completed(Long lessonId, AiSummaryContent content) {
        return new AiSummaryApiResponse(lessonId, content, "COMPLETED");
    }

    // 팩토리 메서드: 진행 중 / 대기 중 / 실패
    public static AiSummaryApiResponse processing(Long lessonId, String status) {
        return new AiSummaryApiResponse(lessonId, null, status);
    }

    // 팩토리 메서드: 요청된 적 없음
    public static AiSummaryApiResponse notFound(Long lessonId) {
        return new AiSummaryApiResponse(lessonId, null, "NOT_FOUND");
    }
}

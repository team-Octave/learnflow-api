package com.teamexp.learnflowapi.ai.dto;

import com.teamexp.learnflowapi.ai.model.AiSummaryContent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiSummaryApiResponse {

    private Long lessonId;
    private String status;       // COMPLETED, PROCESSING, READY, FAILED, NOT_FOUND
    private AiSummaryContent content;

    // 1. 성공 (요약 내용 포함)
    public static AiSummaryApiResponse completed(Long lessonId, AiSummaryContent content) {
        return AiSummaryApiResponse.builder()
            .lessonId(lessonId)
            .status("COMPLETED")
            .content(content)
            .build();
    }

    // 2. 진행 중 (READY, PROCESSING)
    public static AiSummaryApiResponse processing(Long lessonId, String currentStatus) {
        return AiSummaryApiResponse.builder()
            .lessonId(lessonId)
            .status(currentStatus) // "READY" or "PROCESSING"
            .content(null)
            .build();
    }

    // 3. 실패 (에러 해결을 위해 이 메서드가 필요함!)
    public static AiSummaryApiResponse failed(Long lessonId) {
        return AiSummaryApiResponse.builder()
            .lessonId(lessonId)
            .status("FAILED")
            .content(null)
            .build();
    }

    // 4. 요청된 적 없음 (DB에 데이터 없음)
    public static AiSummaryApiResponse notFound(Long lessonId) {
        return AiSummaryApiResponse.builder()
            .lessonId(lessonId)
            .status("NOT_FOUND")
            .content(null)
            .build();
    }

    // 5. 시작되지 않음 (혹시 필요할 경우를 대비해)
    public static AiSummaryApiResponse notStarted() {
        return AiSummaryApiResponse.builder()
            .lessonId(null)
            .status("NOT_STARTED")
            .content(null)
            .build();
    }
}

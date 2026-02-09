package com.teamexp.learnflowapi.ai.service;

import com.teamexp.learnflowapi.ai.dto.AiSummaryApiResponse;
import com.teamexp.learnflowapi.ai.repository.AiSummaryRepository;
import com.teamexp.learnflowapi.ai.repository.AiTaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private final AiSummaryRepository summaryRepository;
    private final AiTaskRepository taskRepository;

    @Transactional(readOnly = true)
    public AiSummaryApiResponse getSummary(Long lessonId) {
        // 1. 결과 테이블(AiSummary) 먼저 확인 (Happy Path)
        return summaryRepository.findByLessonId(lessonId)
            .map(summary -> AiSummaryApiResponse.completed(
                summary.getLessonId(),
                summary.getContent()))

            // 2. 결과가 없다면, 작업 테이블(AiTask) 상태 확인
            .orElseGet(() -> checkProcessingStatus(lessonId));
    }

    private AiSummaryApiResponse checkProcessingStatus(Long lessonId) {
        return taskRepository.findByLessonId(lessonId)
            .map(task -> switch (task.getStatus()) {
                case FAILED -> AiSummaryApiResponse.failed(); // 실패 상태 명시
                default -> AiSummaryApiResponse.processing(lessonId, task.getStatus().name());
            })
            // 3. 둘 다 없으면 "요청된 적 없음"
            .orElse(AiSummaryApiResponse.notFound(lessonId));
    }
}

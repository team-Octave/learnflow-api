package com.teamexp.learnflowapi.ai.service;

import com.teamexp.learnflowapi.ai.dto.AiSummaryApiResponse;
import com.teamexp.learnflowapi.ai.repository.AiContentRepository;
import com.teamexp.learnflowapi.ai.repository.AiSummaryRepository;
import com.teamexp.learnflowapi.ai.repository.AiTaskRepository;
import com.teamexp.learnflowapi.lecture.exception.LessonNotFoundException;
import com.teamexp.learnflowapi.lecture.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiSummaryService {

    private final AiContentRepository contentRepository;
    private final AiSummaryRepository summaryRepository;
    private final AiTaskRepository taskRepository;
    private final LessonRepository lessonRepository;

    /**
     * 강의의 AI 요약 상태 및 결과 조회
     * Flow:
     * 1. Lesson 존재 확인 (Fail -> 404 Exception)
     * 2. AiContent(신 시스템 결과) 확인 (Success -> COMPLETED Response)
     * 3. AiSummary(레거시 결과) 확인 (Success -> COMPLETED Response)
     * 4. AiTask(진행 중인 작업) 확인 (Present -> PROCESSING/FAILED Response)
     * 5. None (Empty -> NOT_FOUND Response implies "Create button needed")
     */
    @Transactional(readOnly = true)
    public AiSummaryApiResponse getSummary(Long lessonId) {
        // 1. 유효한 강의인지 먼저 검증
        if (!lessonRepository.existsById(lessonId)) {
            throw new LessonNotFoundException();
        }

        // 2. 신 시스템 결과 테이블(AiContent) 확인
        var contentResult = contentRepository.findByLessonId(lessonId);
        if (contentResult.isPresent()) {
            return AiSummaryApiResponse.completed(
                lessonId,
                contentResult.get().getSummaryContent()
            );
        }

        // 3. 레거시 결과 테이블(AiSummary) 확인 (하위 호환)
        var summaryResult = summaryRepository.findByLessonId(lessonId);
        if (summaryResult.isPresent()) {
            return AiSummaryApiResponse.completed(
                summaryResult.get().getLessonId(),
                summaryResult.get().getContent()
            );
        }

        // 4. 결과가 없다면, 진행 중인 작업 상태(AiTask) 확인
        return checkProcessingStatus(lessonId);
    }

    private AiSummaryApiResponse checkProcessingStatus(Long lessonId) {
        return taskRepository.findByLessonId(lessonId)
            .map(task -> switch (task.getStatus()) {
                case FAILED -> AiSummaryApiResponse.failed(lessonId);
                case COMPLETED -> AiSummaryApiResponse.notFound(lessonId); // 결과 테이블 미저장 상태
                default -> AiSummaryApiResponse.processing(lessonId, task.getStatus().name());
            })
            // 5. 요청된 적 없음 (클라이언트가 '요약 생성' 버튼을 노출해야 함)
            .orElse(AiSummaryApiResponse.notFound(lessonId));
    }
}

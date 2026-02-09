package com.teamexp.learnflowapi.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.ai.dto.AiJobResponse;
import com.teamexp.learnflowapi.ai.dto.AiJobResultRequest;
import com.teamexp.learnflowapi.ai.model.AiSummary;
import com.teamexp.learnflowapi.ai.model.AiSummaryContent;
import com.teamexp.learnflowapi.ai.model.AiTask;
import com.teamexp.learnflowapi.ai.model.TaskStatus;
import com.teamexp.learnflowapi.ai.repository.AiSummaryRepository;
import com.teamexp.learnflowapi.ai.repository.AiTaskRepository;
import com.teamexp.learnflowapi.content.external.GcpSignedUrlService;
import com.teamexp.learnflowapi.content.model.ContentMedia;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiWorkService {

    private final AiTaskRepository aiTaskRepository;
    private final AiSummaryRepository aiSummaryRepository;
    private final ContentMediaRepository contentMediaRepository;
    private final LessonRepository lessonRepository;
    private final GcpSignedUrlService gcpSignedUrlService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;

    private static final int SIGNED_URL_EXPIRATION_SEC = 3600;

    public List<AiJobResponse> fetchPendingTasks(int limit) {
        // 1. [DB Transaction] SKIP LOCKED로 작업 선점 (락 충돌 시 대기하지 않고 건너뜀)
        List<AiTask> tasks = transactionTemplate.execute(status -> {
            List<AiTask> readyTasks = aiTaskRepository.findTasksToProcess(TaskStatus.READY, PageRequest.of(0, limit));
            for (AiTask task : readyTasks) {
                task.changeStatus(TaskStatus.PROCESSING);
            }
            return readyTasks;
        });

        if (tasks == null || tasks.isEmpty()) {
            return Collections.emptyList();
        }

        List<AiJobResponse> responseList = new ArrayList<>();

        // 2. [No Transaction] 외부 API 호출 (GCP)
        for (AiTask task : tasks) {
            try {
                ContentMedia media = contentMediaRepository.findByLessonId(task.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Media not found"));
                Lesson lesson = lessonRepository.findById(task.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));

                String signedUrl = gcpSignedUrlService.generateDownloadUrl(media.getFileKey(), SIGNED_URL_EXPIRATION_SEC);

                responseList.add(new AiJobResponse(task.getId(), signedUrl, lesson.getLessonTitle()));
                log.info("AI Worker에게 작업 할당: taskId={}, lessonId={}", task.getId(), task.getLessonId());

            } catch (Exception e) {
                log.error("작업 할당 실패 (Retry 처리): taskId={}", task.getId(), e);
                // 즉시 FAILED가 아니라 Retry 로직 수행
                handleAllocationFailure(task.getId());
            }
        }
        return responseList;
    }

    // 할당 실패 시 재시도 처리용 (별도 트랜잭션)
    private void handleAllocationFailure(Long taskId) {
        try {
            transactionTemplate.execute(status -> {
                aiTaskRepository.findById(taskId).ifPresent(this::handleFailure);
                return null;
            });
        } catch (Exception ex) {
            log.error("재시도 상태 업데이트 실패: taskId={}", taskId, ex);
        }
    }

    @Transactional
    public void processResult(AiJobResultRequest request) {
        AiTask task = aiTaskRepository.findById(request.taskId())
            .orElseThrow(() -> new RuntimeException("Task not found"));

        if (request.success()) {
            try {
                AiSummaryContent content = objectMapper.readValue(request.summaryJson(), AiSummaryContent.class);
                if (!aiSummaryRepository.existsByLessonId(task.getLessonId())) {
                    aiSummaryRepository.save(new AiSummary(task.getLessonId(), content));
                }
                task.changeStatus(TaskStatus.COMPLETED);
                log.info("AI 작업 완료: taskId={}", task.getId());
            } catch (Exception e) {
                log.error("결과 저장 실패", e);
                handleFailure(task);
            }
        } else {
            log.warn("AI 작업 실패 보고: taskId={}", task.getId());
            handleFailure(task);
        }
    }

    private void handleFailure(AiTask task) {
        task.incrementRetryCount();
        if (task.getRetryCount() > 3) {
            task.changeStatus(TaskStatus.FAILED);
            log.error("최대 재시도 초과 -> FAILED: taskId={}", task.getId());
        } else {
            long waitMinutes = switch (task.getRetryCount()) {
                case 1 -> 1;
                case 2 -> 5;
                default -> 60;
            };
            task.setNextAttemptAt(Instant.now().plus(waitMinutes, ChronoUnit.MINUTES));
            task.changeStatus(TaskStatus.READY);
            log.info("재시도 예약: taskId={}, count={}", task.getId(), task.getRetryCount());
        }
    }
}

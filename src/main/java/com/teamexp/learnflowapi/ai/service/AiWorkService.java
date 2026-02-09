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

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
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

    // AI 워커가 다운로드하기 위한 URL 만료 시간 (1시간)
    private static final int SIGNED_URL_EXPIRATION_SEC = 3600;

    @Transactional
    public List<AiJobResponse> fetchPendingTasks(int limit) {
        List<AiTask> tasks = aiTaskRepository.findTasksToProcess(TaskStatus.READY, PageRequest.of(0, limit));
        List<AiJobResponse> responseList = new ArrayList<>();

        for (AiTask task : tasks) {
            try {
                task.changeStatus(TaskStatus.PROCESSING);

                ContentMedia media = contentMediaRepository.findByLessonId(task.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Media not found"));
                Lesson lesson = lessonRepository.findById(task.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));

                // streamingCreateSignedUrl의 두 번째 인자는 원래 durationSec이지만,
                // AI 워커용 다운로드 URL 생성을 위해 만료 시간(TTL)으로 3600초를 전달함.
                String signedUrl = gcpSignedUrlService.streamingCreateSignedUrl(media.getFileKey(), SIGNED_URL_EXPIRATION_SEC);

                responseList.add(new AiJobResponse(task.getId(), signedUrl, lesson.getLessonTitle()));
                log.info("AI Worker에게 작업 할당: taskId={}, lessonId={}", task.getId(), task.getLessonId());

            } catch (Exception e) {
                log.error("작업 할당 중 오류 발생 taskId={}", task.getId(), e);
                task.changeStatus(TaskStatus.FAILED);
            }
        }
        return responseList;
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
                log.info("AI 작업 완료 처리됨: taskId={}", task.getId());

            } catch (Exception e) {
                log.error("결과 저장 실패", e);
                handleFailure(task);
            }
        } else {
            log.warn("AI 작업 실패 보고됨: taskId={}, error={}", task.getId(), request.errorMessage());
            handleFailure(task);
        }
    }

    private void handleFailure(AiTask task) {
        task.incrementRetryCount();
        if (task.getRetryCount() > 3) {
            task.changeStatus(TaskStatus.FAILED);
        } else {
            long waitMinutes = switch (task.getRetryCount()) {
                case 1 -> 1;
                case 2 -> 5;
                default -> 60;
            };
            task.setNextAttemptAt(Instant.now().plus(waitMinutes, ChronoUnit.MINUTES));
            task.changeStatus(TaskStatus.READY);
        }
    }
}

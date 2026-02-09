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
import org.springframework.transaction.support.TransactionTemplate; // ✨ 추가

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
    private final TransactionTemplate transactionTemplate; // ✨ 트랜잭션 제어용 템플릿 추가

    private static final int SIGNED_URL_EXPIRATION_SEC = 3600;

    /**
     * [AI 서버 호출용] 처리할 작업을 조회하여 반환 (Polling 대응)
     *  트랜잭션 범위를 분리하여 DB 커넥션 점유 시간을 최소화함.
     */
    public List<AiJobResponse> fetchPendingTasks(int limit) {
        // 1. [DB Transaction] 작업 선점 및 상태 변경 (아주 빠르게 실행됨)
        // SKIP LOCKED가 적용된 쿼리를 사용하여 동시성 문제 해결
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

        // 2. [No Transaction] 외부 네트워크 통신 (GCP Signed URL 생성)
        // DB 트랜잭션이 이미 끝났으므로, GCP가 느려져도 DB 커넥션을 잡고 있지 않음.
        for (AiTask task : tasks) {
            try {
                // 필요한 데이터 조회 (단순 조회는 트랜잭션 없이도 가능하거나, 짧은 읽기 트랜잭션으로 처리됨)
                ContentMedia media = contentMediaRepository.findByLessonId(task.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Media not found"));
                Lesson lesson = lessonRepository.findById(task.getLessonId())
                    .orElseThrow(() -> new RuntimeException("Lesson not found"));

                // GCP Signed URL 생성 (네트워크 I/O 발생 구간)
                String signedUrl = gcpSignedUrlService.generateDownloadUrl(media.getFileKey(), SIGNED_URL_EXPIRATION_SEC);

                responseList.add(new AiJobResponse(task.getId(), signedUrl, lesson.getLessonTitle()));
                log.info("AI Worker에게 작업 할당: taskId={}, lessonId={}", task.getId(), task.getLessonId());

            } catch (Exception e) {
                log.error("작업 할당 중 오류 발생 (GCP/DB조회 실패): taskId={}", task.getId(), e);
                // 3. [New Transaction] 실패한 작업만 별도 트랜잭션으로 상태 롤백(FAILED) 처리
                markTaskAsFailed(task.getId());
            }
        }
        return responseList;
    }

    // 실패 처리용 별도 트랜잭션 메서드
    private void markTaskAsFailed(Long taskId) {
        try {
            transactionTemplate.execute(status -> {
                aiTaskRepository.findById(taskId).ifPresent(t -> t.changeStatus(TaskStatus.FAILED));
                return null;
            });
        } catch (Exception ex) {
            log.error("실패 상태 업데이트 중 2차 에러 발생: taskId={}", taskId, ex);
        }
    }

    /**
     * [AI 서버 호출용] 작업 결과 처리
     */
    @Transactional
    public void processResult(AiJobResultRequest request) {
        AiTask task = aiTaskRepository.findById(request.taskId())
            .orElseThrow(() -> new RuntimeException("Task not found"));

        if (request.success()) {
            try {
                // JSON 파싱 및 결과 저장
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
            task.changeStatus(TaskStatus.READY); // 다시 READY로 돌려서 나중에 fetch 되게 함
        }
    }
}

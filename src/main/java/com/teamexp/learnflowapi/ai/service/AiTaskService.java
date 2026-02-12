package com.teamexp.learnflowapi.ai.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.teamexp.learnflowapi.ai.dto.*;
import com.teamexp.learnflowapi.ai.exception.AiTaskNotFoundException;
import com.teamexp.learnflowapi.ai.model.*;
import com.teamexp.learnflowapi.ai.repository.AiContentRepository;
import com.teamexp.learnflowapi.ai.repository.AiTaskRepository;
import com.teamexp.learnflowapi.content.exception.MediaNotFoundException;
import com.teamexp.learnflowapi.content.external.GcpSignedUrlService;
import com.teamexp.learnflowapi.content.model.ContentMedia;
import com.teamexp.learnflowapi.content.repository.ContentMediaRepository;
import com.teamexp.learnflowapi.global.response.BaseResponse;
import com.teamexp.learnflowapi.lecture.exception.LessonNotFoundException;
import com.teamexp.learnflowapi.lecture.model.Lesson;
import com.teamexp.learnflowapi.lecture.repository.LessonRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import static com.teamexp.learnflowapi.ai.util.AiDatabaseUtils.isDuplicateKeyError;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.DefaultTransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.context.request.async.DeferredResult;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiTaskService {

    private final AiTaskRepository aiTaskRepository;
    private final AiContentRepository aiContentRepository;
    private final ContentMediaRepository contentMediaRepository;
    private final LessonRepository lessonRepository;
    private final GcpSignedUrlService gcpSignedUrlService;
    private final ObjectMapper objectMapper;
    private final TransactionTemplate transactionTemplate;
    private final PlatformTransactionManager transactionManager;
    private final ScheduledExecutorService aiPollingScheduler;

    private static final int SIGNED_URL_EXPIRATION_SEC = 3600;
    private static final int POLL_INTERVAL_SEC = 2;

    /**
     * Long polling으로 태스크 조회
     * ScheduledFuture는 완료/타임아웃/에러 시 cancel하여 리소스 누수 방지
     */
    public void pollTaskAsync(String workerId, int timeout, DeferredResult<BaseResponse<AiTaskPollResponse>> result) {
        // 즉시 태스크 확인
        AiTaskPollResponse task = tryFetchTask(workerId);
        if (task != null) {
            result.setResult(new BaseResponse<>(task));
            return;
        }

        // 태스크가 없으면 주기적으로 재시도
        long startTime = System.currentTimeMillis();
        long timeoutMs = timeout * 1000L;
        AtomicReference<ScheduledFuture<?>> futureRef = new AtomicReference<>();

        ScheduledFuture<?> future = aiPollingScheduler.scheduleAtFixedRate(() -> {
            try {
                if (result.isSetOrExpired()) {
                    ScheduledFuture<?> f = futureRef.get();
                    if (f != null) {
                        f.cancel(false);
                    }
                    return;
                }

                long elapsed = System.currentTimeMillis() - startTime;
                if (elapsed >= timeoutMs) {
                    result.setResult(new BaseResponse<>(AiTaskPollResponse.empty()));
                    return;
                }

                AiTaskPollResponse fetchedTask = tryFetchTask(workerId);
                if (fetchedTask != null) {
                    result.setResult(new BaseResponse<>(fetchedTask));
                }
            } catch (Exception e) {
                log.error("태스크 폴링 중 오류 발생: workerId={}", workerId, e);
            }
        }, 0, POLL_INTERVAL_SEC, TimeUnit.SECONDS);

        futureRef.set(future);
        result.onCompletion(() -> future.cancel(false));
        result.onTimeout(() -> future.cancel(false));
    }

    /**
     * 처리 가능한 태스크 존재 여부 확인 (락 없이 빠르게 확인)
     * 커넥션 풀 고갈 방지를 위해 트랜잭션 없이 실행
     */
    private boolean hasReadyTask() {
        return aiTaskRepository.existsTaskToProcess(TaskStatus.READY);
    }

    /**
     * 태스크 조회 및 할당 (2단계 쿼리 방식)
     * 1단계: 락 없이 존재 여부 확인 (커넥션 최소화)
     * 2단계: 태스크 있을 때만 트랜잭션으로 할당
     */
    private AiTaskPollResponse tryFetchTask(String workerId) {
        // 1단계: 락 없이 빠르게 존재 여부 확인 (커넥션 풀 고갈 방지)
        if (!hasReadyTask()) {
            return null;
        }

        // 2단계: 태스크가 있을 때만 트랜잭션으로 락 획득 후 할당
        return transactionTemplate.execute(status -> {
            return aiTaskRepository.findOneTaskToProcess(TaskStatus.READY)
                .map(task -> {
                    try {
                        ContentMedia media = contentMediaRepository.findByLessonId(task.getLessonId())
                            .orElseThrow(MediaNotFoundException::new);

                        Lesson lesson = lessonRepository.findById(task.getLessonId())
                            .orElseThrow(LessonNotFoundException::new);

                        String signedUrl = gcpSignedUrlService.generateDownloadUrl(
                            media.getFileKey(), SIGNED_URL_EXPIRATION_SEC);

                        Instant expiresAt = Instant.now().plusSeconds(SIGNED_URL_EXPIRATION_SEC);

                        // 응답 생성에 필요한 데이터를 모두 확보한 뒤 할당하여, 예외 시 롤백으로 좀비 태스크 방지
                        task.assignToWorker(workerId);

                        log.info("AI Worker에게 작업 할당: taskId={}, lessonId={}, workerId={}",
                            task.getId(), task.getLessonId(), workerId);

                        return AiTaskPollResponse.of(
                            task.getId(),
                            task.getLessonId(),
                            signedUrl,
                            expiresAt,
                            lesson.getLessonTitle(),
                            task.getRetryCount(),
                            task.getCreatedAt()
                        );
                    } catch (Exception e) {
                        log.error("작업 할당 실패: taskId={}", task.getId(), e);
                        status.setRollbackOnly();
                        return null;
                    }
                })
                .orElse(null);
        });
    }

    /**
     * 태스크 완료 처리
     */
    @Transactional
    public AiTaskCompleteResponse completeTask(Long taskId, AiTaskCompleteRequest request) {
        AiTask task = aiTaskRepository.findById(taskId)
            .orElseThrow(AiTaskNotFoundException::new);

        // 이미 완료된 태스크
        if (task.getStatus() == TaskStatus.COMPLETED) {
            log.info("이미 완료된 태스크: taskId={}", taskId);
            return AiTaskCompleteResponse.alreadyCompleted(taskId, task.getLessonId());
        }

        // PROCESSING 상태가 아닌 경우
        if (task.getStatus() != TaskStatus.PROCESSING) {
            log.warn("PROCESSING 상태가 아닌 태스크에 완료 시도: taskId={}, status={}",
                taskId, task.getStatus());
            throw new IllegalStateException("Task is not in PROCESSING state");
        }

        // 워커 ID 검증
        if (!request.workerId().equals(task.getWorkerId())) {
            log.warn("워커 ID 불일치: taskId={}, expected={}, actual={}",
                taskId, task.getWorkerId(), request.workerId());
            throw new IllegalStateException("Worker ID mismatch");
        }

        // fullAnalysis를 FullAnalysisContent로 변환
        FullAnalysisContent fullAnalysis;
        try {
            fullAnalysis = objectMapper.convertValue(request.fullAnalysis(), FullAnalysisContent.class);
        } catch (Exception e) {
            log.error("fullAnalysis 변환 실패: taskId={}", taskId, e);
            throw new IllegalArgumentException("Invalid fullAnalysis format", e);
        }

        // AiContent 저장 (동시 요청 시 유니크 제약 위반은 이미 존재로 간주하여 idempotent 처리)
        AiContent content = AiContent.create(
            task.getLessonId(),
            request.durationSeconds(),
            request.durationFormatted(),
            request.transcript(),
            fullAnalysis,
            request.summary(),
            request.modelVersion(),
            request.processingTimeSeconds()
        );
        saveContentInNewTransaction(content, task.getLessonId());

        // 태스크 완료
        task.complete();
        log.info("AI 작업 완료: taskId={}, lessonId={}", taskId, task.getLessonId());

        return AiTaskCompleteResponse.of(taskId, task.getLessonId(), "COMPLETED");
    }

    /**
     * 태스크 실패 처리
     */
    @Transactional
    public AiTaskFailResponse failTask(Long taskId, AiTaskFailRequest request) {
        AiTask task = aiTaskRepository.findById(taskId)
            .orElseThrow(AiTaskNotFoundException::new);

        // 이미 처리된 태스크
        if (task.getStatus() == TaskStatus.COMPLETED || task.getStatus() == TaskStatus.FAILED) {
            log.info("이미 처리된 태스크: taskId={}, status={}", taskId, task.getStatus());
            return AiTaskFailResponse.alreadyProcessed(taskId, task.getStatus().name(), task.getRetryCount());
        }

        // PROCESSING 상태가 아닌 경우
        if (task.getStatus() != TaskStatus.PROCESSING) {
            log.warn("PROCESSING 상태가 아닌 태스크에 실패 시도: taskId={}, status={}",
                taskId, task.getStatus());
            throw new IllegalStateException("Task is not in PROCESSING state");
        }

        // 에러 정보 설정
        task.setError(request.errorCode(), request.errorMessage());
        task.clearWorker();

        // 재시도 여부 결정
        if (!request.isRetryable()) {
            task.changeStatus(TaskStatus.FAILED);
            log.warn("재시도 불가 실패: taskId={}, errorCode={}", taskId, request.errorCode());
            return AiTaskFailResponse.of(taskId, "FAILED", task.getRetryCount(), null);
        }

        task.incrementRetryCount();

        if (task.isRetryLimitExceeded()) {
            task.changeStatus(TaskStatus.FAILED);
            log.error("최대 재시도 초과 -> FAILED 처리: taskId={}", taskId);
            return AiTaskFailResponse.of(taskId, "FAILED", task.getRetryCount(), null);
        }

        // 재시도 예약
        long waitMinutes = switch (task.getRetryCount()) {
            case 1 -> 1;
            case 2 -> 5;
            default -> 60;
        };
        Instant nextAttemptAt = Instant.now().plus(waitMinutes, ChronoUnit.MINUTES);
        task.setNextAttemptAt(nextAttemptAt);
        task.changeStatus(TaskStatus.READY);

        log.info("재시도 예약 완료: taskId={}, retryCount={}, nextAttemptAt={}",
            taskId, task.getRetryCount(), nextAttemptAt);

        return AiTaskFailResponse.of(taskId, "READY", task.getRetryCount(), nextAttemptAt);
    }

    /**
     * 하트비트 처리
     */
    @Transactional
    public AiTaskHeartbeatResponse heartbeat(Long taskId, AiTaskHeartbeatRequest request) {
        AiTask task = aiTaskRepository.findById(taskId)
            .orElseThrow(AiTaskNotFoundException::new);

        // PROCESSING 상태가 아닌 경우 (취소됨)
        if (task.getStatus() != TaskStatus.PROCESSING) {
            log.info("처리 중이 아닌 태스크에 하트비트: taskId={}, status={}",
                taskId, task.getStatus());
            return AiTaskHeartbeatResponse.stopProcessing("TASK_NOT_PROCESSING");
        }

        // 워커 ID 불일치 (다른 워커가 처리 중)
        if (!request.workerId().equals(task.getWorkerId())) {
            log.warn("워커 ID 불일치 하트비트: taskId={}, expected={}, actual={}",
                taskId, task.getWorkerId(), request.workerId());
            return AiTaskHeartbeatResponse.stopProcessing("WORKER_MISMATCH");
        }

        // 레슨 삭제 확인
        if (!lessonRepository.existsById(task.getLessonId())) {
            log.info("레슨이 삭제됨, 태스크 취소: taskId={}, lessonId={}",
                taskId, task.getLessonId());
            task.changeStatus(TaskStatus.CANCELLED);
            return AiTaskHeartbeatResponse.stopProcessing("LESSON_DELETED");
        }

        // 하트비트 업데이트
        task.updateHeartbeat(request.currentStep(), request.progress());
        log.debug("하트비트 업데이트: taskId={}, step={}, progress={}",
            taskId, request.currentStep(), request.progress());

        return AiTaskHeartbeatResponse.continueProcessing();
    }

    /**
     * AiContent 저장을 별도 트랜잭션(REQUIRES_NEW)에서 실행하여,
     * 중복 키 예외 시 외부 트랜잭션이 rollback-only로 마킹되지 않도록 함.
     */
    private void saveContentInNewTransaction(AiContent content, Long lessonId) {
        DefaultTransactionDefinition def = new DefaultTransactionDefinition();
        def.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        new TransactionTemplate(transactionManager, def).execute(status -> {
            try {
                aiContentRepository.saveAndFlush(content);
                return null;
            } catch (DataIntegrityViolationException e) {
                if (isDuplicateKeyError(e)) {
                    log.info("AiContent already exists for lessonId={}, treating as success", lessonId);
                    return null;
                }
                throw e;
            }
        });
    }
}

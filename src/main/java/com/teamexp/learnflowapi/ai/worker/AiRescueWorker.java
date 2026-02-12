package com.teamexp.learnflowapi.ai.worker;

import com.teamexp.learnflowapi.ai.model.AiTask;
import com.teamexp.learnflowapi.ai.model.TaskStatus;
import com.teamexp.learnflowapi.ai.repository.AiTaskRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiRescueWorker {

    private final AiTaskRepository aiTaskRepository;

    private static final int ZOMBIE_THRESHOLD_MINUTES = 10;

    /**
     * 좀비 태스크 복구
     * - 하트비트가 10분 이상 없는 PROCESSING 태스크를 감지
     * - 재시도 횟수에 따라 READY 또는 FAILED로 변경
     */
    @Scheduled(fixedDelay = 600000)
    @Transactional
    public void rescueZombies() {
        Instant heartbeatThreshold = Instant.now().minus(ZOMBIE_THRESHOLD_MINUTES, ChronoUnit.MINUTES);

        // 하트비트 기반으로 좀비 감지 (신 시스템)
        List<AiTask> zombies = new java.util.ArrayList<>(
            aiTaskRepository.findByStatusAndLastHeartbeatAtBefore(TaskStatus.PROCESSING, heartbeatThreshold)
        );

        // 하트비트가 null인 경우도 좀비로 처리 (구 시스템 호환)
        Instant updatedAtThreshold = Instant.now().minus(ZOMBIE_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
        zombies.addAll(
            aiTaskRepository.findByStatusAndLastHeartbeatAtIsNullAndUpdatedAtBefore(TaskStatus.PROCESSING, updatedAtThreshold)
        );

        if (zombies.isEmpty()) {
            return;
        }

        log.warn("좀비 작업 {}개 발견, 복구 시작...", zombies.size());

        for (AiTask zombie : zombies) {
            zombie.incrementRetryCount();
            zombie.clearWorker();

            if (zombie.isRetryLimitExceeded()) {
                zombie.changeStatus(TaskStatus.FAILED);
                log.error("좀비 태스크 최대 재시도 초과 -> FAILED: taskId={}, retryCount={}",
                    zombie.getId(), zombie.getRetryCount());
            } else {
                zombie.changeStatus(TaskStatus.READY);
                zombie.setNextAttemptAt(Instant.now());
                log.info("좀비 태스크 복구 -> READY: taskId={}, retryCount={}",
                    zombie.getId(), zombie.getRetryCount());
            }
        }
    }
}

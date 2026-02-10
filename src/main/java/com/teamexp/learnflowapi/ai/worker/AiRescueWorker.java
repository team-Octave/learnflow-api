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

    @Scheduled(fixedDelay = 600000)
    @Transactional
    public void rescueZombies() {
        // 10분 전
        Instant limitTime = Instant.now().minus(10, ChronoUnit.MINUTES);

        List<AiTask> zombies = aiTaskRepository.findByStatusAndUpdatedAtBefore(
            TaskStatus.PROCESSING,
            limitTime
        );

        if (!zombies.isEmpty()) {
            log.warn("🧟 좀비 작업 {}개 발견! 구조 시작...", zombies.size());
            for (AiTask zombie : zombies) {
                // 무조건 READY로 돌리지 않고, 재시도 횟수 체크
                zombie.incrementRetryCount();

                if (zombie.getRetryCount() > 3) {
                    log.error("Zombie task {} exceeded retry limit. Marking as FAILED.", zombie.getId());
                    zombie.changeStatus(TaskStatus.FAILED);
                } else {
                    zombie.changeStatus(TaskStatus.READY);
                    zombie.setNextAttemptAt(Instant.now()); // 즉시 재시도
                    log.info("Task {} 심폐소생 완료 (READY로 변경, retryCount={})", zombie.getId(), zombie.getRetryCount());
                }
            }
        }
    }
}

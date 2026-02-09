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

    /**
     * 10분마다 실행되어 오랫동안 PROCESSING 상태인 좀비 작업을 구조합니다.
     * (서버가 비정상 종료되어 커밋되지 못한 작업들)
     */
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
                zombie.changeStatus(TaskStatus.READY); // 다시 READY로 변경하여 RelayWorker가 가져가도록 함
                log.info("Task {} 심폐소생 완료 (READY로 변경)", zombie.getId());
            }
        }
    }
}

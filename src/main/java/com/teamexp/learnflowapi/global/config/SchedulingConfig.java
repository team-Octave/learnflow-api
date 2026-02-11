package com.teamexp.learnflowapi.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;

@Configuration
public class SchedulingConfig implements SchedulingConfigurer {

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();

        // 스레드 풀 사이즈 설정 (CPU 코어 수 + 알파 or 예상 동시 작업 수)
        // AI 작업은 I/O Blocking이 길기 때문에 넉넉하게 잡는 게 좋습니다.
        scheduler.setPoolSize(10);

        scheduler.setThreadNamePrefix("ai-worker-");
        scheduler.initialize();

        taskRegistrar.setTaskScheduler(scheduler);
    }
}

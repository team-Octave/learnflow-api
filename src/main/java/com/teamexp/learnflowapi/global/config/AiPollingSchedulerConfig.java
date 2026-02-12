package com.teamexp.learnflowapi.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

@Configuration
public class AiPollingSchedulerConfig {

    private static final int POOL_SIZE = 4;

    @Bean(destroyMethod = "shutdown")
    public ScheduledExecutorService aiPollingScheduler() {
        return Executors.newScheduledThreadPool(POOL_SIZE, r -> {
            Thread t = new Thread(r, "ai-polling-");
            t.setDaemon(false);
            return t;
        });
    }
}

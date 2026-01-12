package com.teamexp.learnflowapi.content.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "videoUploadExecutor")
    public Executor videoUploadExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(20);
        executor.setThreadNamePrefix("video-upload-");
        executor.setAllowCoreThreadTimeOut(true);  // idle 시 스레드 정리
        executor.setKeepAliveSeconds(30);
        executor.initialize();
        return executor;
    }
}

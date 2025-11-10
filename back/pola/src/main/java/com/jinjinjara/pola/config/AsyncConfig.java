package com.jinjinjara.pola.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 비동기 처리 설정
 * OpenSearch 색인 등 시간이 걸리는 작업을 백그라운드에서 처리합니다.
 */
@Configuration
@EnableAsync
public class AsyncConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);          // 기본 스레드 수
        executor.setMaxPoolSize(10);          // 최대 스레드 수
        executor.setQueueCapacity(100);       // 큐 크기
        executor.setThreadNamePrefix("async-opensearch-");
        executor.initialize();
        return executor;
    }
}

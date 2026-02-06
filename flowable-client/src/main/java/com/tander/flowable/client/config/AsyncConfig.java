package com.tander.flowable.client.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "taskExecutorBpmn")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        // Текущая проблемная конфигурация:
        // executor.setCorePoolSize(5);
        // executor.setMaxPoolSize(120);
        // executor.setQueueCapacity(50);

        // Рекомендуемая конфигурация:
     /*   executor.setCorePoolSize(20);          // увеличиваем базовое количество потоков

        executor.setKeepAliveSeconds(120);     // увеличиваем время жизни неиспользуемых потоков
*/
        executor.setCorePoolSize(32);

        // Критически важные настройки:
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(60);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());

        executor.setThreadNamePrefix("Async-");
        executor.initialize();
        return executor;
    }

}

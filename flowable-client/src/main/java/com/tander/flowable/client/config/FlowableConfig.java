package com.tander.flowable.client.config;

import com.tander.flowable.client.FlowableErrorEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class FlowableConfig implements ProcessEngineConfigurationConfigurer {

    @Autowired
    private FlowableErrorEventListener errorListener;

    @Override
    public void configure(SpringProcessEngineConfiguration config) {
        config.setAsyncExecutorActivate(true);
        config.setAsyncExecutorNumberOfRetries(3);
        config.setAsyncExecutorSecondsToWaitOnShutdown(30);
       // config.setEventListeners(List.of(errorListener));
    }

    /*@Bean()
    public RestResponseFactory restResponseFactory(ObjectMapper objectMapper) {
        RestResponseFactory restResponseFactory = new RestResponseFactory(objectMapper);
        return restResponseFactory;
    }*/

    @Bean
    public org.springframework.core.task.AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(30);
        executor.setQueueCapacity(500);

        executor.setThreadNamePrefix("flowable-task-Executor-");
        executor.setAwaitTerminationSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }

}
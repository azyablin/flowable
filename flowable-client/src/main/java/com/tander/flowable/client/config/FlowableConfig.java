package com.tander.flowable.client.config;

import com.tander.flowable.client.agenda.CustomAgendaFactory;
import com.tander.flowable.client.FlowableErrorEventListener;
import org.flowable.app.spring.SpringAppEngineConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
public class FlowableConfig {

    @Autowired
    private FlowableErrorEventListener errorListener;

    @Autowired
    private CustomAgendaFactory customAgendaFactory;

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration>
    springProcessEngineConfigurer() {
        return configurer -> {
            configurer.setAsyncExecutorActivate(true);
            configurer.setAsyncExecutorNumberOfRetries(3);
            configurer.setAsyncExecutorSecondsToWaitOnShutdown(30);

            configurer.setAgendaFactory(customAgendaFactory);
            configurer.setParallelMultiInstanceAsyncLeave(false);
            //parallelMultiInstanceAsyncLeave
        };
    }

    @Bean
    public EngineConfigurationConfigurer<SpringAppEngineConfiguration> springAppEngineConfigurer(
    ) {
        return configurer -> {

            //  configurer.setCustomMybatisInterceptors(List.of(jobAfterUpdateInterceptor, mybatisPlusInterceptor));
            // configurer.setCustomMybatisMappers(Set.of(JobMapper.class));


        };
    }

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
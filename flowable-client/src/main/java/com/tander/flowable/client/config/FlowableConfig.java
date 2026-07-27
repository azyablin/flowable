package com.tander.flowable.client.config;

import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.tander.flowable.client.FlowableErrorEventListener;
import com.tander.flowable.client.mybatis.interceptor.JobAfterUpdateInterceptor;
import com.tander.flowable.client.mybatis.mapper.JobMapper;
import org.flowable.app.spring.SpringAppEngineConfiguration;
import org.flowable.job.service.JobServiceConfiguration;
import org.flowable.job.service.impl.asyncexecutor.AsyncJobExecutorConfiguration;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.spring.boot.process.ProcessAsync;
import org.flowable.spring.job.service.SpringAsyncExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.List;
import java.util.Set;

@Configuration
public class FlowableConfig {

    @Autowired
    private FlowableErrorEventListener errorListener;

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration>
    springProcessEngineConfigurer(@ProcessAsync SpringAsyncExecutor springAsyncExecutor) {
        return configurer -> {
            configurer.setAsyncExecutorActivate(true);
            configurer.setAsyncExecutorNumberOfRetries(3);
            configurer.setAsyncExecutorSecondsToWaitOnShutdown(30);
        };
    }

    @Bean
    public EngineConfigurationConfigurer<SpringAppEngineConfiguration> springAppEngineConfigurer(@ProcessAsync SpringAsyncExecutor springAsyncExecutor,
                                                                                                 MybatisPlusInterceptor mybatisPlusInterceptor,
                                                                                                 JobAfterUpdateInterceptor jobAfterUpdateInterceptor
    ) {
        return configurer -> {
            configurer.setCustomMybatisInterceptors(List.of(jobAfterUpdateInterceptor, mybatisPlusInterceptor));
            configurer.setCustomMybatisMappers(Set.of(JobMapper.class));

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
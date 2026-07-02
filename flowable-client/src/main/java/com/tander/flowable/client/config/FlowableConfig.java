package com.tander.flowable.client.config;

import com.tander.flowable.client.FlowableErrorEventListener;
import com.tander.flowable.client.manager.CustomJobEntityManagerImpl;
import com.tander.flowable.client.mybatis.interceptor.JobDeleteInterceptor;
import jakarta.annotation.PostConstruct;
import org.flowable.app.spring.SpringAppEngineConfiguration;
import org.flowable.engine.configurator.ProcessEngineConfigurator;
import org.flowable.job.service.JobServiceConfiguration;
import org.flowable.job.service.impl.asyncexecutor.AsyncJobExecutorConfiguration;
import org.flowable.job.service.impl.persistence.entity.JobEntityManagerImpl;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.flowable.spring.boot.process.ProcessAsync;
import org.flowable.spring.job.service.SpringAsyncExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
public class FlowableConfig {

    @Autowired
    private FlowableErrorEventListener errorListener;


    @Bean
    @ProcessAsync
    @Primary
    public AsyncJobExecutorConfiguration customProcessAsyncExecutorConfiguration() {
        var config = new AsyncJobExecutorConfiguration();
        config.setGlobalAcquireLockEnabled(true);
        return config;
    }

    @Bean
    public EngineConfigurationConfigurer<SpringProcessEngineConfiguration>
    springProcessEngineConfigurer(@ProcessAsync SpringAsyncExecutor springAsyncExecutor) {
        return configurer -> {
            configurer.setAsyncExecutorActivate(true);
            configurer.setAsyncExecutorNumberOfRetries(3);
            configurer.setAsyncExecutorSecondsToWaitOnShutdown(30);
        //    configurer.setCustomMybatisInterceptors(List.of(new JobDeleteInterceptor()));

            springAsyncExecutor.getConfiguration().setGlobalAcquireLockPrefix("flowable-lock");
            JobServiceConfiguration jobservConfig = configurer.getJobServiceConfiguration();
            //  jobservConfig.setJobEntityManager(new CustomJobEntityManagerImpl(jobservConfig, jobservConfig.getJobDataManager()));
        };
    }

    @Bean
    public EngineConfigurationConfigurer<SpringAppEngineConfiguration> springAppEngineConfigurer(@ProcessAsync SpringAsyncExecutor springAsyncExecutor) {
        return configurer -> {

         //   configurer.setCustomMybatisInterceptors(List.of(new JobDeleteInterceptor()));

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

    //@Bean
    public CustomJobEntityManagerImpl customJobEntityManager(SpringProcessEngineConfiguration springProcessEngineConfiguration,
                                                             PlatformTransactionManager transactionManager) {

        var config = springProcessEngineConfiguration.getJobServiceConfiguration();
        var customJobEntityManager = new CustomJobEntityManagerImpl(transactionManager, springProcessEngineConfiguration.getJobServiceConfiguration(),
            springProcessEngineConfiguration.getJobServiceConfiguration().getJobDataManager());
        config.setJobEntityManager(customJobEntityManager);
        return customJobEntityManager;


    }

    @Bean
    public JobServiceConfiguration jobServiceConfiguration(SpringProcessEngineConfiguration springProcessEngineConfiguration) {
        return springProcessEngineConfiguration.getJobServiceConfiguration();
    }


}
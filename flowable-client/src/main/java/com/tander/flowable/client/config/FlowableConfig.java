package com.tander.flowable.client.config;

import com.tander.flowable.client.FlowableErrorEventListener;
import com.tander.flowable.client.action.TransactionalExecutor;
import com.tander.flowable.client.config.properties.AppFlowableProperties;
import com.tander.flowable.client.interceptor.RollbackExceptionInterceptor;
import org.flowable.common.engine.impl.AbstractEngineConfiguration;
import org.flowable.common.engine.impl.EngineConfigurator;
import org.flowable.common.engine.impl.history.HistoryLevel;
import org.flowable.common.spring.SpringTransactionInterceptor;
import org.flowable.job.service.impl.asyncexecutor.AsyncExecutor;
import org.flowable.job.service.impl.asyncexecutor.DefaultAsyncJobExecutor;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.EngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(AppFlowableProperties.class)
public class FlowableConfig implements EngineConfigurationConfigurer<SpringProcessEngineConfiguration>, EngineConfigurator {

    @Autowired
    private FlowableErrorEventListener errorListener;

    @Autowired
    private TransactionalExecutor transactionalExecutor;

    @Lookup
    public AppFlowableProperties appFlowableProperties() {
        return null;
    }

    @Override
    public void configure(SpringProcessEngineConfiguration config) {
        var asyncExecutorProp = appFlowableProperties().getAsyncExecutorOrDefault();





        config.addConfigurator(this);
        config.setAsyncExecutorActivate(asyncExecutorProp.activate());
        config.setAsyncExecutorNumberOfRetries(3);
        config.setDatabaseSchemaUpdate("true");
        config.setHistoryLevel(HistoryLevel.FULL);
        config.setAsyncExecutorSecondsToWaitOnShutdown(asyncExecutorProp.secondsToWaitOnShutdown());
         config.setAsyncExecutorCorePoolSize(80);
        config.setAsyncExecutorMaxPoolSize(40);
        AsyncExecutor asyncExecutor = config.getAsyncExecutor();
        if (asyncExecutor instanceof DefaultAsyncJobExecutor defaultAsyncJobExecutor) {
            defaultAsyncJobExecutor.setMaxAsyncJobsDuePerAcquisition(100);
            defaultAsyncJobExecutor.setGlobalAcquireLockEnabled(true);

        }

        //asyncExecutorMaxAsyncJobsDuePerAcquisition
        // config.setEventListeners(List.of(errorListener));
    }

    @Override
    public void beforeInit(AbstractEngineConfiguration config) {

    }

    @Override
    public void configure(AbstractEngineConfiguration config) {
        var interceptors = config.getCommandInterceptors();
        for (int i = 0; i < interceptors.size(); i++) {
            if (interceptors.get(i) instanceof SpringTransactionInterceptor) {
                interceptors.add(i, new RollbackExceptionInterceptor(transactionalExecutor));
                break;
            }
        }
        config.initInterceptorChain(interceptors);
    }

    @Override
    public int getPriority() {
        return 0;
    }

    /*@Bean()
    public RestResponseFactory restResponseFactory(ObjectMapper objectMapper) {
        RestResponseFactory restResponseFactory = new RestResponseFactory(objectMapper);
        return restResponseFactory;
    }*/

   /* @Bean
    public org.springframework.core.task.AsyncTaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(40);
        executor.setQueueCapacity(500);

        executor.setThreadNamePrefix("flowable-task-Executor-");
        executor.setAwaitTerminationSeconds(30);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAllowCoreThreadTimeOut(true);
        executor.initialize();
        return executor;
    }*/

   /* public ProcessEngineConfiguration processEngineConfiguration() {
        SpringProcessEngineConfiguration.
    }*/

}
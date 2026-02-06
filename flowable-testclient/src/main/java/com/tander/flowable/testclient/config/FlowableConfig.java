package com.tander.flowable.testclient.config;


import com.tander.flowable.testclient.FlowableErrorEventListener;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class FlowableConfig implements ProcessEngineConfigurationConfigurer {

    @Autowired
    private FlowableErrorEventListener errorListener;

    @Override
    public void configure(SpringProcessEngineConfiguration config) {
        config.setAsyncExecutorActivate(true);
        config.setAsyncExecutorNumberOfRetries(3);
        config.setAsyncExecutorSecondsToWaitOnShutdown(30);
        config.setEventListeners(List.of(errorListener));
    }


}
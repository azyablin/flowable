package com.tander.flowable.client.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.tander.flowable.client.FlowableErrorEventListener;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.rest.service.api.RestResponseFactory;
import org.flowable.spring.SpringProcessEngineConfiguration;
import org.flowable.spring.boot.ProcessEngineConfigurationConfigurer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
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
       // config.setEventListeners(List.of(errorListener));
    }

    @Bean()
    public RestResponseFactory restResponseFactory(ObjectMapper objectMapper) {
        RestResponseFactory restResponseFactory = new RestResponseFactory(objectMapper);
        return restResponseFactory;
    }

}
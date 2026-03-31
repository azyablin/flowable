package com.tander.flowable.client.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConstructorBinding;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.stereotype.Component;

import java.util.Optional;

@ConfigurationProperties("app.flowable")
@ConstructorBinding
public record AppFlowableProperties(@DefaultValue AsyncExecutor asyncExecutor) {

    @ConstructorBinding
    public record AsyncExecutor(@DefaultValue("true") Boolean activate, @DefaultValue("30") long secondsToWaitOnShutdown) {

    }

    public AsyncExecutor getAsyncExecutorOrDefault() {
        return asyncExecutor != null ? asyncExecutor : new AsyncExecutor(true, 30);
    }
}

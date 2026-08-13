package com.tander.flowable.client.delegate;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

@Component("initMultipleProcessDelegate")
@RequiredArgsConstructor
public class InitMultipleProcessDelegate implements JavaDelegate {

    private final RuntimeService runtimeService;

    @Override
    public void execute(DelegateExecution execution) {
        var businessKeyStr = Optional
            .ofNullable(execution.getVariable("businessKey", String.class))
            .filter(StringUtils::hasLength)
            .orElseGet(() -> UUID.randomUUID().toString());
        runtimeService.updateBusinessKey(execution.getProcessInstanceId(), businessKeyStr);
    }
}

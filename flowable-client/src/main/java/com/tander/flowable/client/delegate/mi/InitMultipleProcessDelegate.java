package com.tander.flowable.client.delegate.mi;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

import static com.tander.flowable.client.constant.MiConstant.BUSINESS_KEY_VARIABLE_NAME;
import static com.tander.flowable.client.constant.MiConstant.COLLECTION_VARIABLE_NAME;
import static com.tander.flowable.client.constant.MiConstant.DELEGATE_VARIABLE_NAME;
import static com.tander.flowable.client.constant.MiConstant.TRANSIENT_COLLECTION_VARIABLE_NAME;

@Component("initMultipleProcessDelegate")
@RequiredArgsConstructor
public class InitMultipleProcessDelegate implements JavaDelegate {

    private final RuntimeService runtimeService;

    private final ApplicationContext applicationContext;

    @Override
    public void execute(DelegateExecution execution) {
        var businessKeyStr = Optional
            .ofNullable(execution.getVariable(BUSINESS_KEY_VARIABLE_NAME, String.class))
            .filter(StringUtils::hasLength)
            .orElseGet(() -> UUID.randomUUID().toString());
        execution.setTransientVariable(TRANSIENT_COLLECTION_VARIABLE_NAME, execution.getVariable(COLLECTION_VARIABLE_NAME));
        execution.removeVariable(COLLECTION_VARIABLE_NAME);
        Optional.ofNullable(execution.getVariable(DELEGATE_VARIABLE_NAME, String.class))
                .ifPresent(delegate -> applicationContext.getBean(delegate, JavaDelegate.class).execute(execution));
        execution.setVariable(BUSINESS_KEY_VARIABLE_NAME, businessKeyStr);
    }

}

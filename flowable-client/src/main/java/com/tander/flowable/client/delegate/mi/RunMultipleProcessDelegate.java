package com.tander.flowable.client.delegate.mi;

import lombok.RequiredArgsConstructor;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.tander.flowable.client.constant.MiConstant.BUSINESS_KEY_VARIABLE_NAME;
import static com.tander.flowable.client.constant.MiConstant.PARENT_ID_VARIABLE_NAME;


@Component("runMultipleProcessDelegate")
@RequiredArgsConstructor
public class RunMultipleProcessDelegate implements JavaDelegate {

    private ThreadPoolExecutor executor =  new ThreadPoolExecutor(5, 90,
        0L, TimeUnit.MILLISECONDS,
        new LinkedBlockingQueue<>());

    private static final Set<String> SERVICE_FIELDS = Set.of("inputItemVarNameForProcess",
        "processDefinitionKey", "collection", "businessKey");

    private final RuntimeService runtimeService;


    @Override
    public void execute(DelegateExecution execution) {

        var inputItemVarNameForProcessStr =
            Optional.ofNullable(execution.getVariable("inputItemVarNameForProcess", String.class))
                .filter(StringUtils::hasLength)
                .orElse("input_data");

        Map<String, Object> variables = new HashMap<>(execution.getVariablesLocal()
            .entrySet()
            .stream()
            .filter(stringObjectEntry -> !SERVICE_FIELDS.contains(stringObjectEntry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        variables.putAll(Map.of(
            inputItemVarNameForProcessStr, execution.getVariable("item"),
            PARENT_ID_VARIABLE_NAME, execution.getProcessInstanceId()
        ));

        var businessKey = execution.getVariable(BUSINESS_KEY_VARIABLE_NAME, String.class);
        var processDefinitionKey = execution.getVariable("processDefinitionKey", String.class);

        executor.execute(() -> runtimeService
            .createProcessInstanceBuilder()
            .processDefinitionKey(processDefinitionKey)
            .variables(variables)
            .businessKey(businessKey)
            .start());

    }

}
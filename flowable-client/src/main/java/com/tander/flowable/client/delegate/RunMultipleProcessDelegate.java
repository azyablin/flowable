package com.tander.flowable.client.delegate;

import lombok.RequiredArgsConstructor;
import org.flowable.common.engine.api.delegate.Expression;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component("runMultipleProcessDelegate")
@RequiredArgsConstructor
public class RunMultipleProcessDelegate implements JavaDelegate {

    private static final Set<String> SERVICE_FIELDS = Set.of("inputItemVarNameForProcess",
        "processDefinitionKey", "collection", "businessKey");

    private final RuntimeService runtimeService;


    @Override
    public void execute(DelegateExecution execution) {
        var processInstance = ExecutionEntity.class.cast(execution).getProcessInstance();

        var inputItemVarNameForProcessStr =
            Optional.ofNullable(execution.getVariable("inputItemVarNameForProcess", String.class))
                .filter(StringUtils::hasLength)
                .orElse("input_data");

        Map<String, Object> variables = new HashMap<>(processInstance.getVariablesLocal()
            .entrySet()
            .stream()
            .filter(stringObjectEntry -> !SERVICE_FIELDS.contains(stringObjectEntry.getKey()))
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue)));
        variables.putAll(Map.of(
            inputItemVarNameForProcessStr, execution.getVariable("item"),
            "parent_id", execution.getProcessInstanceId()
        ));

        runtimeService
            .createProcessInstanceBuilder()
            .processDefinitionKey(execution.getVariable("processDefinitionKey").toString())
            .variables(variables)
            .businessKey(execution.getProcessInstanceBusinessKey())
            .startAsync();
    }

}
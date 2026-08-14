package com.tander.flowable.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.api.FlowableOptimisticLockingException;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.impl.context.Context;
import org.flowable.common.engine.impl.interceptor.CommandConfig;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.runtime.Execution;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static com.tander.flowable.client.constant.MiConstant.PARENT_ID_VARIABLE_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiInstanceEntityService {


    private final ManagementService managementService;

    private final RuntimeService runtimeService;

    @Transactional
    @Async
    public void processCompleted(FlowableEngineEntityEvent event) {
        if (event.getEntity() instanceof ExecutionEntity executionEntity) {
            Optional.ofNullable(executionEntity.getVariable(PARENT_ID_VARIABLE_NAME, String.class))
                .filter(parentId -> isAllInstancesCompleted(parentId, executionEntity.getBusinessKey()))
                .map(this::getReceiveExecution)
                .ifPresent(this::trigger);
        }
    }

    private boolean isAllInstancesCompleted(String parentId, String businessKey) {
        return runtimeService
            .createProcessInstanceQuery()
            .processInstanceBusinessKey(businessKey)
            .variableValueEquals(PARENT_ID_VARIABLE_NAME, parentId)
            .count() == 0;
    }

    private Execution getReceiveExecution(String parentId) {
        return runtimeService.createExecutionQuery()
            .processInstanceId(parentId)
            .activityId("endReceiveTask")
            .singleResult();
    }

    private void trigger(Execution receiveExecution) {
        CommandConfig config = new CommandConfig().transactionRequiresNew(); // Новая транзакция
        managementService.executeCommand(config, commandContext -> {
            try {
                runtimeService.trigger(receiveExecution.getId());
            } catch (FlowableObjectNotFoundException | FlowableOptimisticLockingException e) {
                log.debug("Процесс уже завершён {}", receiveExecution.getId());
            }
            return null;
        });

    }


}

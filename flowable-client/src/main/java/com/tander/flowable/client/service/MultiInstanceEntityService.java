package com.tander.flowable.client.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.api.FlowableOptimisticLockingException;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.runtime.Execution;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import static com.tander.flowable.client.constant.MiConstant.PARENT_ID_VARIABLE_NAME;

@Service
@RequiredArgsConstructor
@Slf4j
public class MultiInstanceEntityService {


    private final ManagementService managementService;

    private final TransactionalService transactionalService;

    private final RuntimeService runtimeService;

    private static final Map<String, Integer> COMPLETED_EXECUTIONS = new ConcurrentHashMap<>();


    public void processCompleted(FlowableEngineEntityEvent event) {
        if (event.getEntity() instanceof ExecutionEntity executionEntity) {
            Optional.ofNullable(executionEntity.getVariable(PARENT_ID_VARIABLE_NAME, String.class))
                .filter(parentId -> isAllInstancesCompleted(parentId, executionEntity.getBusinessKey()))
                .map(this::getReceiveExecution)
                .ifPresent(this::trigger);
        }
    }

    private boolean isAllInstancesCompleted(String parentId, String businessKey) {
        var count = runtimeService
            .createProcessInstanceQuery()
            .processInstanceBusinessKey(businessKey)
            .variableValueEquals(PARENT_ID_VARIABLE_NAME, parentId)
            .count();
        if (count > 0) {
            log.info("count > 0");
        }
        return count == 0;
    }

    private Execution getReceiveExecution(String parentId) {
        return runtimeService.createExecutionQuery()
            .processInstanceId(parentId)
            .activityId("endReceiveTask")
            .singleResult();
    }

    private void trigger(Execution receiveExecution) {
        var processInstanceId = receiveExecution.getProcessInstanceId();
        transactionalService.executeAsync(() ->
            managementService.executeCommand(commandContext -> {
                try (var locker = new ProcessInstanceLocker(commandContext, processInstanceId)) {
                    Optional.ofNullable(getReceiveExecution(processInstanceId)).ifPresent(execution -> {
                        try {
                            var value = COMPLETED_EXECUTIONS.compute(receiveExecution.getId(), (s, oldValue) ->
                                Optional.ofNullable(oldValue).map(integer -> integer++).orElse(0));
                            if (value > 0) {
                                log.info("value added");
                            }

                            runtimeService.trigger(receiveExecution.getId());
                        } catch (FlowableObjectNotFoundException | FlowableOptimisticLockingException e) {
                            log.debug("Процесс уже завершён {}", receiveExecution.getId());
                        }
                    });
                } catch (FlowableOptimisticLockingException e) {
                    log.debug("Процесс уже завершён {}", receiveExecution.getId());
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                return null;
            })
        );


    }

    public static class ProcessInstanceLocker implements AutoCloseable {

        private final CommandContext commandContext;
        private String processInstanceId;
        private ExecutionEntityManager executionEntityManage;

        public ProcessInstanceLocker(CommandContext commandContext, String processInstanceId) {
            this.commandContext = commandContext;
            this.executionEntityManage = CommandContextUtil.getExecutionEntityManager(commandContext);
            this.processInstanceId = processInstanceId;
            var date = new Date();
            date.setTime(date.getTime() + 10000);
            var lockOwner = "MultiInstanceEntityService.trigger";
            executionEntityManage.updateProcessInstanceLockTime(processInstanceId, lockOwner, date);
        }

        @Override
        public void close() throws Exception {
            executionEntityManage.clearProcessInstanceLockTime(processInstanceId);
        }
    }


}

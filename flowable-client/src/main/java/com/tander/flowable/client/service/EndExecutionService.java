package com.tander.flowable.client.service;

import com.tander.flowable.client.util.ProcessInstanceLocker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.api.FlowableOptimisticLockingException;
import org.flowable.common.engine.impl.cfg.TransactionState;
import org.flowable.common.engine.impl.context.Context;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.common.engine.impl.persistence.entity.Entity;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.runtime.ExecutionQuery;
import org.flowable.variable.api.persistence.entity.VariableInstance;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;

@Service
@Slf4j
public class EndExecutionService {


    @Lookup
    public ManagementService managementService() {
        return null;
    }


    @Lookup
    public RuntimeService runtimeService() {
        return null;
    }


    private static final String COMPLETED_VAR_NAME = "completed";
    private static final String CHILD_EXECUTION_ID = "childExecutionId";


    public void completeCallActivity(DelegateExecution execution) {
        SubProcessInfo subProcessInfo = getSubProcessInfo((ExecutionEntity) execution, "nrOfInstances");
        subProcessInfo.subProcessEntity().setVariableLocal(COMPLETED_VAR_NAME, true);
        subProcessInfo.subProcessEntity().setVariableLocal(CHILD_EXECUTION_ID, execution.getId());
        Context.getTransactionContext().addTransactionListener(TransactionState.COMMITTED, commandContext ->
            leave(subProcessInfo)
        );
    }

    private void leave(SubProcessInfo subProcessInfo) {
        var processInstanceId = subProcessInfo.parentProcessEntity().getId();
        managementService().executeCommand(commandContext -> {
            ExecutionQuery query = runtimeService()
                .createExecutionQuery()
                .parentId(subProcessInfo.parentProcessEntity().getId())
                .variableValueEquals(COMPLETED_VAR_NAME, true);


            var completed = query.count();
            var nrOfInstances = getLoopVariable(subProcessInfo);
            if (completed >= nrOfInstances) {
                try (var locker = new ProcessInstanceLocker(commandContext, processInstanceId)) {
                    query
                        .list()
                        .stream()
                        .map(ExecutionEntity.class::cast)
                        .map(executionEntity -> executionEntity.getVariableLocal(CHILD_EXECUTION_ID, String.class))
                        .map(id -> runtimeService().createExecutionQuery().executionId(id).singleResult())
                        .map(exec -> exec.getId())
                        .forEach(id -> {
                            log.info("finish ececution id = {}", id);
                            runtimeService().trigger(id);
                        });
                } catch (FlowableOptimisticLockingException | FlowableObjectNotFoundException e) {
                    log.debug("Процесс уже завершён {}", processInstanceId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
    }


    private SubProcessInfo getSubProcessInfo(ExecutionEntity execution, String variableName) {
        ExecutionEntity subProcessEntity = execution;
        VariableInstance variable = execution.getVariableInstanceLocal(variableName);
        ExecutionEntity parent = Optional.ofNullable(execution.getSuperExecution())
            .orElse(execution.getParent());
        Optional<SubProcessInfo> subProcessInfo = Optional.empty();
        while (variable == null && parent != null) {
            variable = parent.getVariableInstanceLocal(variableName);
            subProcessInfo = Objects.isNull(variable) ? subProcessInfo : Optional.of(new SubProcessInfo(parent, subProcessEntity, variable));
            subProcessEntity = parent;
            parent = Optional.ofNullable(parent.getSuperExecution())
                .orElse(parent.getParent());
        }
        return subProcessInfo.orElseThrow(() -> new RuntimeException("Базовый процесс не найден"));
    }

    private Integer getLoopVariable(SubProcessInfo subProcessInfo) {
        return (Integer) subProcessInfo.variableInstance().getValue();
    }

    record SubProcessInfo(ExecutionEntity parentProcessEntity, ExecutionEntity subProcessEntity,
                          VariableInstance variableInstance) {

    }


}

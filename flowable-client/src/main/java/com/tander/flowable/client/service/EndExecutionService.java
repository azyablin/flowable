package com.tander.flowable.client.service;

import com.tander.flowable.client.util.ProcessInstanceLocker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.api.FlowableOptimisticLockingException;
import org.flowable.common.engine.impl.cfg.TransactionState;
import org.flowable.common.engine.impl.context.Context;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.ManagementService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Service;

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


    public void completeCallActivity(ExecutionEntity superExecutionEntity, ExecutionEntity childProcessInstanceExecutionEntity) {
        superExecutionEntity.setVariableLocal(COMPLETED_VAR_NAME, true);
        superExecutionEntity.setVariableLocal(CHILD_EXECUTION_ID, childProcessInstanceExecutionEntity.getId());
        Context.getTransactionContext().addTransactionListener(TransactionState.COMMITTED, commandContext ->
            {
                leave(superExecutionEntity);
            }
        );
    }

    private void leave(ExecutionEntity superExecutionEntity) {
        managementService().executeCommand(commandContext -> {
            FlowableEngineAgenda agenda = CommandContextUtil.getAgenda(commandContext);
            var completed = runtimeService()
                .createExecutionQuery()
                .parentId(superExecutionEntity.getParentId())
                .variableValueEquals(COMPLETED_VAR_NAME, true)
                .count();
            var nrOfInstances = superExecutionEntity.getParent().getVariableLocal("nrOfInstances", Integer.class);
            if (completed >= nrOfInstances) {
                var processInstanceId = superExecutionEntity.getProcessInstanceId();
                try (var locker = new ProcessInstanceLocker(commandContext, processInstanceId)) {
                    runtimeService()
                        .createExecutionQuery()
                        .parentId(superExecutionEntity.getParentId())
                        .variableValueEquals(COMPLETED_VAR_NAME, true)
                        .list()
                        .stream()
                        .map(ExecutionEntity.class::cast)
                        .map(executionEntity -> executionEntity.getVariableLocal(CHILD_EXECUTION_ID, String.class))
                        .map(id -> CommandContextUtil.getExecutionEntityManager(commandContext).findById(id))
                        .forEach(executionEntity -> agenda.planEndExecutionOperationSynchronous(executionEntity));
                } catch (FlowableOptimisticLockingException | FlowableObjectNotFoundException e) {
                    log.debug("Процесс уже завершён {}", processInstanceId);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            return null;
        });
    }


}

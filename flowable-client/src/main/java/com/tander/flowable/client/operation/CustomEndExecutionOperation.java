package com.tander.flowable.client.operation;

import com.tander.flowable.client.service.EndExecutionService;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.agenda.EndExecutionOperation;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;

@Slf4j
public class CustomEndExecutionOperation extends EndExecutionOperation {

    private final EndExecutionService endExecutionService;

    public CustomEndExecutionOperation(CommandContext commandContext, ExecutionEntity execution, EndExecutionService endExecutionService) {
        super(commandContext, execution);
        this.endExecutionService = endExecutionService;
    }

    @Override
    protected void scheduleAsyncCompleteCallActivity(ExecutionEntity superExecutionEntity, ExecutionEntity childProcessInstanceExecutionEntity) {
         endExecutionService.completeCallActivity(superExecutionEntity, childProcessInstanceExecutionEntity);
        //  super.scheduleAsyncCompleteCallActivity(superExecutionEntity, childProcessInstanceExecutionEntity);
    }

    public CustomEndExecutionOperation(CommandContext commandContext, ExecutionEntity execution, boolean forceSynchronous, EndExecutionService endExecutionService) {
        super(commandContext, execution, forceSynchronous);
        this.endExecutionService = endExecutionService;
    }
}

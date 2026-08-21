package com.tander.flowable.client.agenda;

import com.tander.flowable.client.operation.CustomEndExecutionOperation;
import com.tander.flowable.client.service.EndExecutionService;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.agenda.DefaultFlowableEngineAgenda;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.springframework.stereotype.Component;


public class CustomAgenda  extends DefaultFlowableEngineAgenda {

    private final EndExecutionService endExecutionService;

    public CustomAgenda(CommandContext commandContext, EndExecutionService endExecutionService) {
        super(commandContext);
        this.endExecutionService = endExecutionService;
    }

    @Override
    public void planEndExecutionOperation(ExecutionEntity execution) {
        CustomEndExecutionOperation operation = new CustomEndExecutionOperation(commandContext, execution, endExecutionService);
        planOperation(operation);
    }

    @Override
    public void planEndExecutionOperationSynchronous(ExecutionEntity execution) {
        CustomEndExecutionOperation operation = new CustomEndExecutionOperation(commandContext, execution, true, endExecutionService);
        planOperation(operation);
    }


}


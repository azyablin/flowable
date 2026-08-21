package com.tander.flowable.client.agenda;

import com.tander.flowable.client.service.EndExecutionService;
import lombok.RequiredArgsConstructor;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.FlowableEngineAgenda;
import org.flowable.engine.FlowableEngineAgendaFactory;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CustomAgendaFactory implements FlowableEngineAgendaFactory  {

    private final EndExecutionService endExecutionService;

    @Override
    public FlowableEngineAgenda createAgenda(CommandContext commandContext) {
        return new CustomAgenda(commandContext, endExecutionService);
    }

}

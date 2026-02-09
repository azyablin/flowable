package com.tander.flowable.client.listener;


import com.tander.flowable.client.service.BpmExecutionService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("processListener")
@RequiredArgsConstructor
public class ProcessListener implements ExecutionListener {

    private final BpmExecutionService bpmExecutionService;

   /* @Override
    public void execute(DelegateExecution execution) {
        bpmExecutionService.addExecution(execution.getId());
    }*/

    @Override
    public void notify(DelegateExecution execution) {
        bpmExecutionService.addExecution(execution.getId());
    }
}
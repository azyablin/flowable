package com.tander.flowable.client.action;


import com.tander.flowable.client.service.BpmExecutionService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("processAction")
@RequiredArgsConstructor
public class ProcessAction implements JavaDelegate {

    private final BpmExecutionService bpmExecutionService;

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("ProcessAction run");
    //    bpmExecutionService.addExecution(execution.getId());
    }
}
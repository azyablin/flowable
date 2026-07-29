package com.tander.flowable.client.listener;


import com.tander.flowable.client.service.BpmExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("processListener")
@RequiredArgsConstructor
@Slf4j
public class ProcessListener implements ExecutionListener {

    private final BpmExecutionService bpmExecutionService;

   /* @Override
    public void execute(DelegateExecution execution) {
        bpmExecutionService.addExecution(execution.getId());
    }*/

    @SneakyThrows
    @Override
    public void notify(DelegateExecution execution) {
        bpmExecutionService.addExecution(execution.getId());
        log.info("process instance created");
    }
}
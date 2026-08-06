package com.tander.flowable.client.listener;


import com.tander.flowable.client.service.BpmExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component("subProcessListener")
@RequiredArgsConstructor
@Slf4j
public class SubProcessListener implements ExecutionListener {
    private final BpmExecutionService bpmExecutionService;


    @SneakyThrows
    @Override
    public void notify(DelegateExecution execution) {
        bpmExecutionService.addExecution(execution.getId());
        log.info("sub process instance created");
    }
}
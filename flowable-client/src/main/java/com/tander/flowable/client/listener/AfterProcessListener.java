package com.tander.flowable.client.listener;


import com.tander.flowable.client.service.AsyncResponseService;
import com.tander.flowable.client.service.BpmExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component("afterProcessListener")
@RequiredArgsConstructor
@Slf4j
public class AfterProcessListener implements ExecutionListener {

    private final BpmExecutionService bpmExecutionService;

   /* @Override
    public void execute(DelegateExecution execution) {
        bpmExecutionService.addExecution(execution.getId());
    }*/

    @Override
    public void notify(DelegateExecution execution) {

        AsyncResponseService.AFTER_EXECUTIONS.put(execution.getId(), "t");
        log.info("process event exit");
        if (!AsyncResponseService.EXECUTIONS.containsKey(execution.getId())) {
            log.info("execution id not sended {} ", execution.getId());
        }
    }

}
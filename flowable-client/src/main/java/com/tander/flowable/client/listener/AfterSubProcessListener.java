package com.tander.flowable.client.listener;


import com.tander.flowable.client.service.AsyncResponseService;
import com.tander.flowable.client.service.BpmExecutionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component("afterSudProcessListener")
@RequiredArgsConstructor
@Slf4j
public class AfterSubProcessListener implements ExecutionListener {



    @Override
    public void notify(DelegateExecution execution) {
       log.info("sub process event exit");
    }

}
package com.tander.flowable.client.listener;

import com.tander.flowable.client.service.EndExecutionService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component("finishSubProcess")
@RequiredArgsConstructor
public class FinishSubProcessListener implements ExecutionListener {

    private final EndExecutionService endExecutionService;

    @Override
    public void notify(DelegateExecution execution) {
        endExecutionService.completeCallActivity(execution);
    }

}

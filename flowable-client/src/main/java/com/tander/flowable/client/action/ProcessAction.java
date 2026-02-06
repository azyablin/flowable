package com.tander.flowable.client.action;

import com.tander.flowable.client.service.HazelcastService;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("processAction")
@RequiredArgsConstructor
public class ProcessAction implements JavaDelegate {

    private final HazelcastService hazelcastService;

    @Override
    public void execute(DelegateExecution execution) {
        hazelcastService.addExecution(execution.getId());
    }
}
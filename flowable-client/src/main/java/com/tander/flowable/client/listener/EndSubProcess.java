package com.tander.flowable.client.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;

@Slf4j
public class EndSubProcess implements ExecutionListener {



    @Override
    public void notify(DelegateExecution execution) {
        log.info("sub process exit");
    }

}
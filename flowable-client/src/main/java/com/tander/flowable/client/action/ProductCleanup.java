package com.tander.flowable.client.action;

import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;


@Component("productCleanup")
@Slf4j
public class ProductCleanup implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("process {} завершён", execution.getId());
    }

}
package com.tander.flowable.client.action;

import com.tander.flowable.client.service.BpmProcessService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;


@Component("productError")
@Slf4j
@RequiredArgsConstructor
public class ProductError implements JavaDelegate {

    private final BpmProcessService processService;

    @Override
    public void execute(DelegateExecution execution) {
        processService.finishProcess(execution.getProcessInstanceId());
        log.info("process {} завершён с ошибкой", execution.getId());
    }

}
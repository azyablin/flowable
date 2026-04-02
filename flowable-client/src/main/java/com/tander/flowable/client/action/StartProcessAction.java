package com.tander.flowable.client.action;

import com.tander.flowable.client.model.ActionErrorInfo;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

@Component("startProcessAction")
@Slf4j
public class StartProcessAction extends BaseAction {

    @Override
    public void internalExecute(DelegateExecution execution) {
        log.info("StartProcessAction " + execution.getProcessInstanceId());
    }

    @Override
    public void handleActionErrorInfo(DelegateExecution execution, ActionErrorInfo actionErrorInfo) {

    }
}

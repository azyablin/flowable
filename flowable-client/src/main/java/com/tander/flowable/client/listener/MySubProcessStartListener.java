package com.tander.flowable.client.listener;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.ExecutionListener;
import org.springframework.stereotype.Component;

@Component("mySubProcessStartListener")
public class MySubProcessStartListener implements ExecutionListener {
    @Override
    public void notify(DelegateExecution execution) {
        String processInstanceId = execution.getProcessInstanceId();
        String executionId = execution.getId();
        // Можно получить переменные
        Object variable = execution.getVariable("someKey");
        System.out.println("ExecutionListener: Старт подпроцесса. " +
            "ID процесса: " + processInstanceId +
            ", ID выполнения: " + executionId);
    }
}
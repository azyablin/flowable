package com.tander.flowable.client.util;

import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;

import java.util.Date;

public class ProcessInstanceLocker  implements AutoCloseable {

    private final CommandContext commandContext;
    private String processInstanceId;
    private ExecutionEntityManager executionEntityManage;

    public ProcessInstanceLocker(CommandContext commandContext, String processInstanceId) {
        this.commandContext = commandContext;
        this.executionEntityManage = CommandContextUtil.getExecutionEntityManager(commandContext);
        this.processInstanceId = processInstanceId;
        var date = new Date();
        date.setTime(date.getTime() + 10000);
        var lockOwner = "ProcessInstanceLocker";
        executionEntityManage.updateProcessInstanceLockTime(processInstanceId, lockOwner, date);
    }

    @Override
    public void close() throws Exception {
        executionEntityManage.clearProcessInstanceLockTime(processInstanceId);
    }
}
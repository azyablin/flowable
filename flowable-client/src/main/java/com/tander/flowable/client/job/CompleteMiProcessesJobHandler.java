package com.tander.flowable.client.job;

import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.job.service.JobHandler;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.variable.api.delegate.VariableScope;

public class CompleteMiProcessesJobHandler implements JobHandler {

    public static final String TYPE = "complete-mi-processes-type";

    @Override
    public String getType() {
        return "";
    }

    @Override
    public void execute(JobEntity job, String configuration, VariableScope variableScope, CommandContext commandContext) {

    }
}

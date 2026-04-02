package com.tander.flowable.client.action;

import com.tander.flowable.client.interceptor.RollbackExceptionInterceptor;
import com.tander.flowable.client.model.ActionErrorInfo;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;

public abstract class BaseAction implements JavaDelegate  {

    @Override
    public final void execute(DelegateExecution execution) {
       //execution.getProcessInstanceId()

    /*    var actionErrorInfo = RollbackExceptionInterceptor.getActionErrorInfo();
        if (actionErrorInfo.isPresent()) {
            handleActionErrorInfo(execution, actionErrorInfo.get());
            return;
        }
        try {
            internalExecute(execution);
        } catch (Exception e) {
            RollbackExceptionInterceptor.setActionErrorInfo(new ActionErrorInfo(this, e));
            throw e;
        }*/
        internalExecute(execution);
    }

    public abstract void internalExecute(DelegateExecution execution);

    public abstract void handleActionErrorInfo(DelegateExecution execution, ActionErrorInfo actionErrorInfo);

}

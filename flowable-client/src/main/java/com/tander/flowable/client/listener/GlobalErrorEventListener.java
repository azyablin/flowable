package com.tander.flowable.client.listener;

import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.delegate.event.FlowableErrorEvent;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class GlobalErrorEventListener implements FlowableEventListener {

    @Override
    public void onEvent(FlowableEvent event) {
        if ("JOB_EXECUTION_FAILURE".equals(event.getType().name())) {
            log.info("JOB_EXECUTION_FAILURE {} ", event);
        }
       log.info("event type " + event.getType().name());
            /*FlowableErrorEvent errorEvent = (FlowableErrorEvent) event;
            log.error("Глобальная ошибка Flowable: processId={}, activityId={}, errorCode={}",
                errorEvent.getProcessInstanceId(),
                errorEvent.getActivityId(),
                errorEvent.getErrorCode());
        }*/
    }

    @Override
    public boolean isFailOnException() {
        return false;
    }

    @Override
    public boolean isFireOnTransactionLifecycleEvent() {
        return false;
    }

    @Override
    public String getOnTransaction() {
        return null;
    }
}
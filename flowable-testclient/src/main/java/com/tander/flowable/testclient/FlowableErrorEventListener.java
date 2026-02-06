package com.tander.flowable.testclient;

import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.springframework.stereotype.Component;

@Component
public class FlowableErrorEventListener implements FlowableEventListener {


    @Override
    public void onEvent(FlowableEvent event) {
        System.out.println(event);
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
        return "";
    }
}
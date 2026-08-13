package com.tander.flowable.client.listener;


import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEventListener;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableProcessStartedEvent;
import org.springframework.stereotype.Component;

//@Component
@RequiredArgsConstructor
public class MyProcessStartedListener extends AbstractFlowableEngineEventListener {

    private final RuntimeService runtimeService;

    @Override
    public void onEvent(FlowableEvent event) {
        if (event instanceof FlowableProcessStartedEvent) {
            FlowableProcessStartedEvent startedEvent = (FlowableProcessStartedEvent) event;

            // Здесь ваша логика, например, логирование или отправка уведомления
            System.out.println("Процесс запущен: " + startedEvent.toString());
        }
    }

    @Override
    protected void processCompleted(FlowableEngineEntityEvent event) {
        super.processCompleted(event);
    }

    @Override
    public String getOnTransaction() {
        return "COMMITTED"; // Указываем момент срабатывания
    }

    @PostConstruct
    public void init() {
        runtimeService.addEventListener(this);
    }

}
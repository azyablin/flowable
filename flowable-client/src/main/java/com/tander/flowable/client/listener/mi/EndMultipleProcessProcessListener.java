package com.tander.flowable.client.listener.mi;

import com.tander.flowable.client.service.MultiInstanceEntityService;
import lombok.RequiredArgsConstructor;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.impl.cfg.TransactionState;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.springframework.stereotype.Component;

@Component
public class EndMultipleProcessProcessListener {

    public EndMultipleProcessProcessListener(ProcessEngine processEngine,
                                             MultiInstanceEntityService multiInstanceEntityService) {
        processEngine.getProcessEngineConfiguration()
            .getEventDispatcher()
            .addEventListener(new AfterTranListener(multiInstanceEntityService),
                FlowableEngineEventType.PROCESS_COMPLETED);

    }

    @RequiredArgsConstructor
    public static class AfterTranListener extends AbstractFlowableEngineEventListener {

        private final MultiInstanceEntityService multiInstanceEntityService;

        @Override
        protected void processCompleted(FlowableEngineEntityEvent event) {
            multiInstanceEntityService.processCompleted(event);
        }

        @Override
        public boolean isFireOnTransactionLifecycleEvent() {
            return true;
        }

        @Override
        public String getOnTransaction() {
            return TransactionState.COMMITTED.name();
        }


    }


}

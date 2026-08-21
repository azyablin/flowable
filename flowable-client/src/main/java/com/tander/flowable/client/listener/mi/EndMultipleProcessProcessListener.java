package com.tander.flowable.client.listener.mi;

import com.tander.flowable.client.service.MultiInstanceEntityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEntityEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEvent;
import org.flowable.common.engine.api.delegate.event.FlowableEngineEventType;
import org.flowable.common.engine.impl.cfg.TransactionState;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.delegate.event.AbstractFlowableEngineEventListener;
import org.flowable.engine.delegate.event.FlowableActivityEvent;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class EndMultipleProcessProcessListener {

    public EndMultipleProcessProcessListener(ProcessEngine processEngine,
                                             MultiInstanceEntityService multiInstanceEntityService) {
        processEngine.getProcessEngineConfiguration()
            .getEventDispatcher()
            .addEventListener(new AfterTranListener(multiInstanceEntityService),
                FlowableEngineEventType.PROCESS_COMPLETED, FlowableEngineEventType.ACTIVITY_STARTED);

    }

    @RequiredArgsConstructor
    public static class AfterTranListener extends AbstractFlowableEngineEventListener {


        private final MultiInstanceEntityService multiInstanceEntityService;


        @Override
        protected void activityStarted(FlowableActivityEvent event) {
            log.info("Activity Name = " + event.getActivityId());
            super.activityStarted(event);
        }

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

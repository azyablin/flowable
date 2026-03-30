package com.tander.flowable.client.service;

import javax.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityManager;
import org.flowable.engine.impl.util.CommandContextUtil;
import org.flowable.engine.runtime.Execution;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncResponseService {

    public static final Map<String, String> AFTER_EXECUTIONS = new ConcurrentHashMap<>();
    public static final Map<String, Integer> EXECUTIONS = new ConcurrentHashMap<>();

    public static final AtomicInteger COUNTER = new AtomicInteger();

    private final RuntimeService runtimeService;
    private final ProcessEngine processEngine;

    public static void init() {
        AFTER_EXECUTIONS.clear();
        EXECUTIONS.clear();
        COUNTER.set(0);
    }




    @Async("taskExecutorBpmn")
    public void sendCompletionSignal(String executionId) {
        sendCompletionSignal(executionId, true);
    }


    private void sendCompletionSignal(String executionId, boolean repeatOnError) {

        try {


            Map<String, Object> signalVariables = new HashMap<>();

            if (!repeatOnError) {
                System.out.println("repeatOnError " + executionId);
                internalSendSignal(executionId);
               // runtimeService.signalEventReceived("ASYNC_RESPONSE_SIGNAL", executionId, signalVariables);
            } else {
                EXECUTIONS.put(executionId, 0);
                COUNTER.incrementAndGet();
                internalSendSignal( executionId);
               // runtimeService.signalEventReceived("ASYNC_RESPONSE_SIGNAL", executionId, signalVariables);
            }
            var cnt = COUNTER.get();
            if (cnt >= 100) {
                System.out.printf("test");
            }
            log.info("send executionId: {}, count: {} ", executionId, COUNTER.get());


        } catch (Exception e) {
            if (repeatOnError) {
                sendCompletionSignal(executionId, false);
                return;
            }
            log.error("AsyncResponseService: Ошибка отправки сигнала: " + executionId, e);

            if (!AFTER_EXECUTIONS.containsKey(executionId)) {
                log.info("executionId не передавался ранее {}", executionId);
            }
            Execution execution = runtimeService.createExecutionQuery()
                .executionId(executionId)
                .singleResult();

            if (execution != null && !execution.isEnded()) {
                log.info("executionId не выполнен {}", executionId);
            }
        }

    }

    private void internalSendSignal(String executionId) {
        runtimeService.trigger(executionId);
    }


}
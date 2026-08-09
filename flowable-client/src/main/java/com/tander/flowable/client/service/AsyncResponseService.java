package com.tander.flowable.client.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandContext;
import org.flowable.engine.ProcessEngine;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.impl.persistence.entity.ExecutionEntity;
import org.flowable.engine.impl.persistence.entity.ExecutionEntityImpl;
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


    //@Async("taskExecutorBpmn")
    public void sendMessage(String messageName, String executionId) {
        runtimeService.messageEventReceived(messageName, executionId);
    }

    public void sendMessage(String messageName, String executionId, Map<String, Object> processVariables) {
        runtimeService.messageEventReceived(messageName, executionId, processVariables);
    }

    @Async("taskExecutorBpmn")
    public void sendMessageAsinc(String messageName, String executionId) {
        for (int i = 0; i < 10; ++i) {
            var execution = runtimeService.createExecutionQuery().executionId(executionId).singleResult();
            if (execution != null) {
                ExecutionEntityImpl e = (ExecutionEntityImpl) execution;
                System.out.println(e.getStartTime());
                runtimeService.messageEventReceived(messageName, executionId);
                break;
            } else {
                log.info("execution not found");
            }

        }
    }

    @Async("taskExecutorBpmn")
    @Transactional
    public void sendMessageByProcess(String messageName, String processInstanceId) {

        for (int i = 0; i < 10; ++i) {
            var list = runtimeService.createEventSubscriptionQuery()
                .eventType("message")
                .eventName(messageName)
                .processInstanceId(processInstanceId)
                .list();
            if (!list.isEmpty()) {
                var executionId = list.get(0).getExecutionId();
                log.info("message received");
                runtimeService.messageEventReceived(messageName, executionId);
                return;
            } else {
                log.info("list is empty");
            }
        }
        throw new RuntimeException("message not received");
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
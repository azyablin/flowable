package com.tander.flowable.client.action;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RuntimeService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Service
@Slf4j
@RequiredArgsConstructor
public class AsyncResponseService {

    private final Map<String, Integer> SENDED_MAP = new ConcurrentHashMap<>();

    private final AtomicInteger counter = new AtomicInteger();

    private final RuntimeService runtimeService;

    private final Map<String, CompletableFuture<Void>> pendingRequests = new ConcurrentHashMap<>();

    @Async("taskExecutorBpmn")
    public void startAsyncProcessing(String executionId, int size) {
        sendCompletionSignal(executionId, size, true);
    }

    public void setItemCount(int count) {
        counter.set(count);
    }

    public int getItemCount() {
       return counter.get();
    }

    private void sendCompletionSignal(String executionId, int size, boolean repeatOnError) {
        try {


            Map<String, Object> signalVariables = new HashMap<>();

            if (!repeatOnError) {
                System.out.println("repeatOnError " + executionId);
                runtimeService.signalEventReceived("ASYNC_RESPONSE_SIGNAL", executionId, signalVariables);
            } else {
                runtimeService.signalEventReceived("ASYNC_RESPONSE_SIGNAL", executionId, signalVariables);
            }

            SENDED_MAP.put(executionId, size);
            System.out.println("AsyncResponseService: Сигнал отправлен для execution: " + executionId + " thread id = "
                + Thread.currentThread().getId() + " size = " + size);

        } catch (Exception e) {
            if (repeatOnError) {
                sendCompletionSignal(executionId, size, false);
                return;
            }
            if (!SENDED_MAP.containsKey(executionId)) {
                System.err.println("AsyncResponseService: Сигнал не был ранее отправлен!!!!!   " + executionId + " " + e.getMessage());

            }
            System.err.println("AsyncResponseService: Ошибка отправки сигнала: " + executionId + " " + e.getMessage());
        }
        counter.decrementAndGet();
    }



}
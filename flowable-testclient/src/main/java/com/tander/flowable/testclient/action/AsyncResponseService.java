package com.tander.flowable.testclient.action;

import org.flowable.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AsyncResponseService {

    @Autowired
    private RuntimeService runtimeService;

    private final Map<String, CompletableFuture<Void>> pendingRequests = new ConcurrentHashMap<>();

    @Async("taskExecutor")
    public void startAsyncProcessing(Map<String, Object> product, String executionId) {
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                System.out.println("AsyncResponseService: Начало асинхронной обработки для товара " + product.get("id"));

                // Имитация длительной обработки (например, вызов внешнего API)
                performAsyncWork(product);

                // После завершения обработки отправляем сигнал
                sendCompletionSignal(executionId, product);

                System.out.println("AsyncResponseService: Асинхронная обработка завершена для товара " + product.get("id"));

            } catch (Exception e) {
                System.err.println("AsyncResponseService: Ошибка при асинхронной обработке: " + e.getMessage());
                // Можно отправить сигнал с ошибкой
                sendErrorSignal(executionId, product, e.getMessage());
            }
        });

        pendingRequests.put(executionId, future);
    }

    private void performAsyncWork(Map<String, Object> product) {
        try {
            // Имитация работы - случайная задержка от 2 до 10 секунд
            int delay = 2000 + new Random().nextInt(8000);
            System.out.println("AsyncResponseService: Имитация работы (" + delay + "ms) для товара " + product.get("id"));
            Thread.sleep(delay);

            // Здесь может быть реальная логика: вызов REST API, обработка файла и т.д.
            product.put("asyncProcessed", true);
            product.put("processedAt", new Date());
            product.put("processingResult", "SUCCESS");

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Асинхронная обработка прервана", e);
        }
    }

    private void sendCompletionSignal(String executionId, Map<String, Object> product) {
        try {
            Thread.sleep(5000L);
            Map<String, Object> signalVariables = new HashMap<>();
            signalVariables.put("asyncResult", "SUCCESS");
            signalVariables.put("processedProduct", product);
            signalVariables.put("completionTime", new Date());

            // Отправляем сигнал для продолжения процесса
            runtimeService.signalEventReceived("ASYNC_RESPONSE_SIGNAL", executionId, signalVariables);

            System.out.println("AsyncResponseService: Сигнал отправлен для execution: " + executionId);

        } catch (Exception e) {
            System.err.println("AsyncResponseService: Ошибка отправки сигнала: " + e.getMessage());
        }
    }

    private void sendErrorSignal(String executionId, Map<String, Object> product, String error) {
        try {
            Map<String, Object> signalVariables = new HashMap<>();
            signalVariables.put("asyncResult", "ERROR");
            signalVariables.put("errorMessage", error);
            signalVariables.put("processedProduct", product);

            runtimeService.signalEventReceived("ASYNC_RESPONSE_SIGNAL", executionId, signalVariables);

        } catch (Exception e) {
            System.err.println("AsyncResponseService: Ошибка отправки сигнала об ошибке: " + e.getMessage());
        }
    }

    // Метод для ручного запуска сигнала (например, из контроллера)
    public void triggerSignalManually(String executionId, Map<String, Object> resultData) {
        try {
            Map<String, Object> signalVariables = new HashMap<>();
            signalVariables.put("asyncResult", "MANUAL_TRIGGER");
            signalVariables.put("manualData", resultData);
            signalVariables.put("triggeredAt", new Date());

            runtimeService.signalEventReceived("ASYNC_RESPONSE_SIGNAL", executionId, signalVariables);

            System.out.println("AsyncResponseService: Ручной сигнал отправлен для execution: " + executionId);

        } catch (Exception e) {
            System.err.println("AsyncResponseService: Ошибка ручной отправки сигнала: " + e.getMessage());
        }
    }

    // Отмена ожидающей обработки
    public void cancelAsyncProcessing(String executionId) {
        CompletableFuture<Void> future = pendingRequests.get(executionId);
        if (future != null && !future.isDone()) {
            future.cancel(true);
            pendingRequests.remove(executionId);
            System.out.println("AsyncResponseService: Обработка отменена для execution: " + executionId);
        }
    }
}
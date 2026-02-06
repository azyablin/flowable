package com.tander.flowable.testclient.action;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component("asyncResponseHandler")
public class AsyncResponseHandler implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        Map<String, Object> product = (Map<String, Object>) execution.getVariable("product");
        String productId = product != null ? (String) product.get("id") : "unknown";

        // Получаем результат асинхронной обработки из переменных сигнала
        String asyncResult = (String) execution.getVariable("asyncResult");
        Map<String, Object> processedProduct = (Map<String, Object>) execution.getVariable("processedProduct");

        System.out.println("AsyncResponseHandler: Обработка после сигнала для товара " + productId);
        System.out.println("AsyncResponseHandler: Результат асинхронной обработки: " + asyncResult);

        if ("SUCCESS".equals(asyncResult)) {
            // Обновляем данные товара результатами обработки
            if (processedProduct != null) {
                product.putAll(processedProduct);
            }
            System.out.println("AsyncResponseHandler: Успешная обработка для товара " + productId);
        } else if ("ERROR".equals(asyncResult)) {
            String errorMessage = (String) execution.getVariable("errorMessage");
            System.err.println("AsyncResponseHandler: Ошибка обработки для товара " + productId + ": " + errorMessage);
            product.put("processingError", errorMessage);
        }

        execution.setVariable("asyncProcessingCompleted", true);
    }
}
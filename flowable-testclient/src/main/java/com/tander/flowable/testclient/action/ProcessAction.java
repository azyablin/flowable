package com.tander.flowable.testclient.action;


import org.flowable.engine.RuntimeService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Random;

@Component("processAction")
public class ProcessAction implements JavaDelegate {

    private final Random random = new Random();

    @Autowired
    private AsyncResponseService asyncResponseService;

    @Autowired
    private RuntimeService runtimeService;

    @Override
    public void execute(DelegateExecution execution) {
        // Получаем текущий товар из контекста multi-instance
        Map<String, Object> product = (Map<String, Object>) execution.getVariable("product");
        Integer productId = Integer.valueOf((String) product.get("id"));

        String executionId = execution.getId();
        String processInstanceId = execution.getProcessInstanceId();

        System.out.println("ProcessAction: Обработка товара " + productId);

        // Определяем действие (для демонстрации - случайный выбор)
        String[] actions = {"WAIT", "NEXT", "FINISH"};


        String selectedAction = actions[productId % actions.length];

        // Сохраняем результат в переменную процесса
        execution.setVariableLocal("actionResult", selectedAction);

        // Для WAIT сценария - запускаем асинхронную обработку
        if ("WAIT".equals(selectedAction)) {
            // Сохраняем идентификаторы для последующего возобновления
            execution.setVariable("waitingExecutionId", executionId);
            execution.setVariable("waitingProductId", productId);

            // Запускаем асинхронную обработку
            asyncResponseService.startAsyncProcessing(product, executionId);
        }

        System.out.println("ProcessAction: Для товара " + productId + " выбрано действие: " + selectedAction);
    }
}
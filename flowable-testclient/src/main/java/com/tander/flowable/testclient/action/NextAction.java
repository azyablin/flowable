package com.tander.flowable.testclient.action;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.Map;


@Component("nextAction")
@Slf4j
public class NextAction implements JavaDelegate {

    @Override
    @SneakyThrows
    public void execute(DelegateExecution execution) {
        log.info(String.valueOf(Thread.currentThread().getId()));
      Thread.sleep(5000L);
        Map<String, Object> product = (Map<String, Object>) execution.getVariable("product");
        String productId = product != null ? (String) product.get("id") : "unknown";

        System.out.println("NextAction: Выполнение NEXT действия для товара " + productId);

        // Дополнительная логика для NEXT
        execution.setVariable("nextActionCompleted", true);
    }

}
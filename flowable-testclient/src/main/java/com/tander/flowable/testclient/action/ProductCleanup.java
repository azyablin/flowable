package com.tander.flowable.testclient.action;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Component("productCleanup")
public class ProductCleanup implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> products = (List<Map<String, Object>>) execution.getVariable("products");

        System.out.println("ProductCleanup: Удаление " + (products != null ? products.size() : 0) + " товаров");

        if (products != null) {
            for (Map<String, Object> product : products) {
                String productId = (String) product.get("id");
                System.out.println("ProductCleanup: Товар " + productId + " удален");
            }
        }

        System.out.println("ProductCleanup: Все товары успешно удалены");
    }
}
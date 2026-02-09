package com.tander.flowable.testclient.action;

import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.Deployment;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;


@Service
public class ProcessTestService {

    private boolean deployed;


    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private RepositoryService repositoryService;

    public void deployAndTestProcess() {
        try {
            // Деплой процесса
            if (!deployed) {
                Deployment deployment = repositoryService.createDeployment()
                    .addClasspathResource("processes/product_processing.bpmn20.xml")
                    .name("Product Processing Deployment")
                    .deploy();

                System.out.println("Процесс успешно задеплоен: " + deployment.getId());
                deployed = true;
            }

            // Создание тестовых данных
            List<Map<String, Object>> products = new ArrayList<>();
            products.add(createProduct("1", "Товар 1"));
            products.add(createProduct("2", "Товар 2"));
         //   products.add(createProduct("3", "Товар 3"));

            // Запуск процесса
            Map<String, Object> variables = new HashMap<>();
            variables.put("products", products);

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                "product_processing",
                variables
            );

            System.out.println("Процесс успешно запущен: " + processInstance.getId());

        } catch (Exception e) {
            System.err.println("Ошибка при деплое/запуске процесса: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private Map<String, Object> createProduct(String id, String name) {
        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("name", name);
        return product;
    }

}
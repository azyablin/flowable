package com.tander.flowable.client.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.repository.ProcessDefinition;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;


@Service
@RequiredArgsConstructor
@Slf4j
public class BpmnProcessService {

    private static final ThreadPoolTaskExecutor EXECUTOR = new ThreadPoolTaskExecutor();

    public static final Integer ITEM_COUNT = 200;

    public static final Integer PROCESS_COUNT = 100;

    public static final Integer TOTAL_COUNT = ITEM_COUNT * PROCESS_COUNT;

    private static final String PROCESS_DEFINITION_KEY = "product_processing_wait";

    private static final String PROCESS_NAME = "Product Processing Deployment With Wait";

    private final RuntimeService runtimeService;

    private final RepositoryService repositoryService;

    static {
        EXECUTOR.setCorePoolSize(5);
        EXECUTOR.setMaxPoolSize(10);          // Максимальное количество потоков
        EXECUTOR.setQueueCapacity(100);       // Размер очереди задач
        EXECUTOR.setKeepAliveSeconds(60);     // Время жизни н
        EXECUTOR.initialize();
    }


    public void deployAndTestProcess() {
        log.info("**************START PROCESSES************");
        try {
            deployProcess(false);
            IntStream.range(0, PROCESS_COUNT)
                .boxed()
                .parallel()
                .forEach(integer -> this.runProcess());

        } catch (Exception e) {
            log.error("Ошибка при деплое/запуске процесса: ", e);
        }
    }

    private void runProcess() {
        try {
            var products = IntStream.range(0, ITEM_COUNT)
                .boxed()
                .toList();

            Map<String, Object> variables = new HashMap<>();
            variables.put("products", products);

            ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
                PROCESS_DEFINITION_KEY,
                variables
            );

            System.out.println("Процесс успешно запущен: " + processInstance.getId() + " thread id = " + Thread.currentThread().getId());

        } catch (Exception e) {
            log.error("Ошибка при запуске процесса: ", e);
        }
    }

    public void deployProcess(boolean ignoreExisting) {
        if (ignoreExisting || !isProcessDeployed()) {
            repositoryService.createDeployment()
                .addClasspathResource("processes/product_processing.with.wait.bpmn20.xml")
                .name(PROCESS_NAME)
                .deploy();
        }
    }

    private Map<String, Object> createProduct(String id, String name) {
        Map<String, Object> product = new HashMap<>();
        product.put("id", id);
        product.put("name", name);
        return product;
    }


    private boolean isProcessDeployed(String processDefinitionKey) {
        ProcessDefinition processDefinition = repositoryService.createProcessDefinitionQuery()
            .processDefinitionKey(processDefinitionKey)
            .latestVersion()
            .singleResult();

        return processDefinition != null;
    }

    private boolean isProcessDeployed() {
        return isProcessDeployed(PROCESS_DEFINITION_KEY);
    }




}
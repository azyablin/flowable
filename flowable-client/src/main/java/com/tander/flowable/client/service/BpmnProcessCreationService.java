package com.tander.flowable.client.service;

import com.tander.flowable.client.model.BpmProcess;
import com.tander.flowable.client.model.CreationProcessesStat;
import com.tander.flowable.client.repository.BpmProcessRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;


@Service
@RequiredArgsConstructor
@Slf4j
public class BpmnProcessCreationService {

    private static final String PROCESS_DEFINITION_KEY = "product_processing_wait_rv";

    private static final String PROCESS_DEFINITION_PATH = "processes/product_processing.with.wait.bpmn20.xml";

    private static final String PROCESS_NAME = "Product Processing Deployment With Wait";

    private final RuntimeService runtimeService;

    private final RepositoryService repositoryService;


    private final TransactionalService transactionalService;

    private final BpmProcessService bpmProcessService;

    private boolean deployed;


    @Value("${server.port}")
    private String serverPort;

    @Async
    public void deployAndRunProcess(int processCount, int itemCount) {
        var startTime = System.currentTimeMillis();
        if (bpmProcessService.count() == 0) {
            transactionalService.execute(() -> bpmProcessService.fillProcessTable(processCount));
        }
        log.info("**************START PROCESSES************");
        try {
            deployed = false;
            deployProcess(false);
            while (true) {
                var info = bpmProcessService.updateProcessTable(() -> runProcess(itemCount));
                if (info.size() == 0) {
                    break;
                }
                Thread.sleep(1000L);
                log.info("Создано процессов {}, порт {} ", info.itemLeft(), serverPort);
            }
        } catch (Exception e) {
            log.error("Ошибка при деплое/запуске процесса: ", e);
        }
        deployed = true;
        log.info("СОЗДАНИЕ ПРОЦЕССОВ ЗАВЕРШЕНО ЗА {} СЕК, ПОРТ {}",
            (System.currentTimeMillis() - startTime)/1000, serverPort);
    }


    private String runProcess(int itemCount) {
        var products = IntStream.range(0, itemCount)
            .boxed()
            .toList();

        Map<String, Object> variables = new HashMap<>();
        variables.put("products", products);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
            PROCESS_DEFINITION_KEY,
            variables
        );
        log.info("Процесс успешно запущен: {}, thread id: {}, порт: {}",
            processInstance.getId(), Thread.currentThread().getId(), serverPort);
        return processInstance.getProcessInstanceId();
    }

    public void deployProcess(boolean ignoreExisting) {
        if (ignoreExisting || deployed) {
            repositoryService.createDeployment()
                .addClasspathResource(PROCESS_DEFINITION_PATH)
                .name(PROCESS_NAME)
                .deploy();
        }
    }


}
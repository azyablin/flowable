package com.tander.flowable.client.service;

import com.tander.flowable.client.model.BpmProcess;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;


@Service
@RequiredArgsConstructor
@Slf4j
public class BpmnProcessCreationService {

    private static final String PROCESS_DEFINITION_KEY = "product_processing_wait_rv2";

    private static final String PROCESS_DEFINITION_PATH = "processes/" + PROCESS_DEFINITION_KEY + ".bpmn";

    private static final String PROCESS_NAME = "Product Processing Deployment With Wait";

    private final RuntimeService runtimeService;

    private final RepositoryService repositoryService;


    private final TransactionalService transactionalService;

    private final BpmProcessService bpmProcessService;

    @Value("${server.port}")
    private String serverPort;

    public void fillProcessTable(int processCount) {
        deployProcess(false);
        transactionalService.execute(() -> bpmProcessService.fillProcessTable(processCount));
    }


    @Async
    @Transactional
    public void runProcess(int itemCount, BpmProcess bpmProcess) {
        var products = IntStream.range(0, itemCount)
            .boxed()
            .toList();

        Map<String, Object> variables = new HashMap<>();
        variables.put("products", products);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
            PROCESS_DEFINITION_KEY,
            variables
        );
        bpmProcess.setProcessId(processInstance.getProcessInstanceId());
        bpmProcessService.save(bpmProcess);
        log.info("Процесс успешно запущен: {}, thread id: {}, порт: {}",
            processInstance.getId(), Thread.currentThread().getId(), serverPort);
    }

    public void deployProcess(boolean ignoreExisting) {
        if (ignoreExisting) {
            repositoryService.createDeployment()
                .addClasspathResource(PROCESS_DEFINITION_PATH)
                .name(PROCESS_NAME)
                .deploy();
        }
    }


}
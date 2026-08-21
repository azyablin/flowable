package com.tander.flowable.client.service;

import com.tander.flowable.client.model.BpmProcess;
import com.tander.flowable.client.model.Product;
import com.tander.flowable.client.repository.ProductRepository;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.api.FlowableObjectNotFoundException;
import org.flowable.common.engine.api.FlowableOptimisticLockingException;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.runtime.Execution;
import org.flowable.engine.runtime.ProcessInstance;
import org.flowable.eventsubscription.api.EventSubscription;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;


@Service
@RequiredArgsConstructor
@Slf4j
public class BpmnProcessCreationService {

    private static final String PROCESS_DEFINITION_KEY = "product_processing_wait_rv";

    private static final String PROCESS_DEFINITION_PATH = "processes/" + PROCESS_DEFINITION_KEY + ".bpmn";

    private static final String SUB_PROCESS_DEFINITION_KEY = "mi_sub_process";

    private static final String SUB_PROCESS_DEFINITION_PATH = "processes/" + SUB_PROCESS_DEFINITION_KEY + ".bpmn";

    private static final String FREE_PROCESS_DEFINITION_KEY = "process_product";

    private static final String FREE_PROCESS_DEFINITION_PATH = "processes/" + FREE_PROCESS_DEFINITION_KEY + ".bpmn";

    private static final String PROCESS_NAME = "Product Processing Deployment With Wait";

    private static final String SUB_PROCESS_NAME_ = "Product Processing Deployment With Wait sub";

    private final RuntimeService runtimeService;

    private final RepositoryService repositoryService;


    private final TransactionalService transactionalService;

    private final BpmProcessService bpmProcessService;

    private final AsyncResponseService asyncResponseService;

    private final ProductRepository productRepository;

    @Autowired
    @Qualifier("taskExecutor")
    private ThreadPoolTaskExecutor executor;

    @Value("${server.port}")
    private String serverPort;

    @Value("${spring.output.ansi.enabled:detect}")
    private AnsiOutput.Enabled springOutputAnsiEnabled;

    public void fillProcessTable(int processCount) {
        deployProcess(false);
        if (bpmProcessService.count() == 0) {
            transactionalService.execute(() -> bpmProcessService.fillProcessTable(processCount));
        }
    }

    private List<Product> createProducts(int itemCount) {
        var id = System.currentTimeMillis()/1000;
        var threadId = Thread.currentThread().getId();
        var products = IntStream.range(0, itemCount)
            .boxed()
            .map(idx -> id + idx)
            .map(idx ->
                new Product().setCode(idx.toString()).setId(idx).setName("pr_" + idx).setUpdateThreadId(threadId)
            )
            .toList();
        return products;
        //return productRepository.saveAll(products);

    }

    @Async
    @Transactional
    public void runProcess(int itemCount, BpmProcess bpmProcess) {
        var products = createProducts(itemCount);

        Map<String, Object> variables = new HashMap<>();
        variables.put("products", products);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey(
            PROCESS_DEFINITION_KEY,
            variables
        );
        bpmProcess.setProcessId(processInstance.getProcessInstanceId());
        bpmProcessService.save(bpmProcess);
     /*   products.forEach(product -> {
            var executionId =  runtimeService.createEventSubscriptionQuery()
                .eventType("message")
                .eventName("event_sub_process")
                .processInstanceId(processInstance.getProcessInstanceId())
                .list().get(0).getExecutionId();
            asyncResponseService.sendMessage("event_sub_process", executionId);
        });*/

     //
        log.info("Процесс успешно запущен: {}, thread id: {}, порт: {}",
            processInstance.getId(), Thread.currentThread().getId(), serverPort);
    }

    public void deployProcess(boolean ignoreExisting) {


        if (ignoreExisting) {
            repositoryService.createDeployment()
                .addClasspathResource(PROCESS_DEFINITION_PATH)
                .name(PROCESS_NAME)
                .deploy();
            repositoryService.createDeployment()
                .addClasspathResource(SUB_PROCESS_DEFINITION_PATH)
                .name(PROCESS_NAME)
                .deploy();

            repositoryService.createDeployment()
                .addClasspathResource(FREE_PROCESS_DEFINITION_PATH)
                .name(PROCESS_NAME)
                .deploy();


        }
    }



}
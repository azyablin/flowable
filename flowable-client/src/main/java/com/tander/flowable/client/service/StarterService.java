package com.tander.flowable.client.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.Collection;
import java.util.HashMap;
import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class StarterService {

    private static final int MAX_THREAD_COUNT = 10;

    private final BpmnProcessCreationService bpmnProcessCreationService;

    private final BpmnSignalService bpmnSignalService;

    private final TaskExecutor taskExecutor;

    private final BpmProcessService bpmProcessService;

    @Autowired
    @Qualifier("taskExecutorBpmn")
    private Executor executor;


    @Value("${process.count:}")
    private Integer initProcessCount;

    @Value("${process.item.count:}")
    private Integer initProcessItemCount;

    @Value("${process.continue:}")
    private Boolean processContinue;

    @Value("${server.port}")
    private String serverPort;


    @EventListener(ApplicationReadyEvent.class)
    public void start() {
        if (!ObjectUtils.isEmpty(initProcessCount) && !ObjectUtils.isEmpty(initProcessItemCount)) {
            taskExecutor.execute(() ->  runProcesses(initProcessCount, initProcessItemCount));
        }

        if (processContinue != null) {
            taskExecutor.execute(() -> bpmnSignalService.continueProcesses());
        }
    }

    @SneakyThrows
    public void runProcesses(int processCount, int itemCount) {
        var startTime = System.currentTimeMillis();
        var tExecutor = (ThreadPoolTaskExecutor)executor;
        bpmnProcessCreationService.fillProcessTable(processCount);
        log.info("**************START PROCESSES************");
        while (true) {
            if (tExecutor.getActiveCount() + tExecutor.getQueueSize() > MAX_THREAD_COUNT) {
                Thread.sleep(100L);
                continue;
            }
            var info = bpmProcessService.getBatch();
            if (info.size() == 0) {
                break;
            }
            info.forEach(bpmProcess -> bpmnProcessCreationService.runProcess(itemCount, bpmProcess));
            Thread.sleep(1000L);
            log.info("Создано процессов {}, порт {} ", bpmProcessService.countByProcessIdNotNull(), serverPort);
        }
        log.info("СОЗДАНИЕ ПРОЦЕССОВ ЗАВЕРШЕНО ЗА {} СЕК, ПОРТ {}",
            (System.currentTimeMillis() - startTime)/1000, serverPort);

    }

}

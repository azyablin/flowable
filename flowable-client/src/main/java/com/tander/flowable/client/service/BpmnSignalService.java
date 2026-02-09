package com.tander.flowable.client.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;

import java.util.concurrent.Executor;

@Service
@RequiredArgsConstructor
@Slf4j
public class BpmnSignalService {

    private static final int MAX_THREAD_COUNT = 10;

    private final BpmExecutionService bpmExecutionService;

    private final AsyncResponseService asyncResponseService;

    @Autowired
    @Qualifier("taskExecutorBpmn")
    private Executor executor;

    @Value("${server.port}")
    private int serverPort;

    @SneakyThrows
    @Async
    public void continueProcesses() {
        AsyncResponseService.init();
        var startTime = System.currentTimeMillis();
        var tExecutor = (ThreadPoolTaskExecutor)executor;
        while (true) {
            if (tExecutor.getActiveCount() + tExecutor.getQueueSize() > MAX_THREAD_COUNT) {
                Thread.sleep(100L);
                continue;
            }
            var info = bpmExecutionService.processIds(asyncResponseService::sendCompletionSignal);
            if (info.size() == 0) {
                break;
            }
            Thread.sleep(1000L);
            log.info("{} items left", info.itemLeft());
        }
        log.info("ОБРАБОТКА ПРОЦЕССОВ ЗАВРШЕНА {} СЕК, ПОРТ {}",
            (System.currentTimeMillis() - startTime)/1000, serverPort);
    }

}

package com.tander.flowable.client.service;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

@Service
@RequiredArgsConstructor
public class StarterService {

    private final BpmnProcessCreationService bpmnProcessCreationService;

    private final BpmnSignalService bpmnSignalService;

    private final TaskExecutor taskExecutor;

    @Value("${process.count:}")
    private Integer processCount;

    @Value("${process.item.count:}")
    private Integer processItemCount;

    @Value("${process.continue:}")
    private Boolean processContinue;


    @PostConstruct
    public void start() {
        if (!ObjectUtils.isEmpty(processCount) && !ObjectUtils.isEmpty(processItemCount)) {
            taskExecutor.execute(() ->  bpmnProcessCreationService.deployAndRunProcess(processCount, processItemCount));
        }

        if (processContinue != null) {
            taskExecutor.execute(() -> bpmnSignalService.continueProcesses());
        }
    }
}

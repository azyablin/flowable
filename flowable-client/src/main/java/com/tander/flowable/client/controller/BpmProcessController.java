package com.tander.flowable.client.controller;

import com.tander.flowable.client.model.CreationProcessesStat;
import com.tander.flowable.client.service.AsyncResponseService;
import com.tander.flowable.client.service.BpmProcessService;
import com.tander.flowable.client.service.BpmnProcessCreationService;
import com.tander.flowable.client.service.BpmnSignalService;
import com.tander.flowable.client.service.StarterService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.Executors;
import java.util.concurrent.locks.LockSupport;

@Slf4j
@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class BpmProcessController {

    private final BpmnProcessCreationService processTestService;

    private final BpmnSignalService bpmnSignalService;

    private final StarterService starterService;

    private final BpmProcessService bpmProcessService;

    private final AsyncResponseService asyncResponseService;

    @Operation
    @GetMapping("/start")
    public void start(@RequestParam int processCount, @RequestParam int itemCount) {
        starterService.runProcesses(processCount, itemCount);
    }

    @Operation
    @GetMapping("/processes/clear")
    public void clearProcesses(@RequestParam int processCount, @RequestParam int itemCount) {
        bpmProcessService.clearProcesses();
    }


    @GetMapping("/deploy")
    public void deploy() {
        processTestService.deployProcess(true);
    }

    @GetMapping("/continue")
    public void continueProcesses() {
        bpmnSignalService.continueProcesses();
    }

    @Operation
    @GetMapping("/sendMessage")
    public void sendMessage(@RequestParam String messageName, @RequestParam String executionId) {
        Runnable runnable = () -> {
            System.out.println("******send message*****");
            try {
                asyncResponseService.sendMessage(messageName, executionId);
            } catch (Exception e) {
                log.info("************* {} *********", e);
                return;
            }
            LockSupport.parkNanos(10^9 * 5);
        };
        var pool = Executors.newFixedThreadPool(5);
        for (int i = 0; i < 5; ++i) {
            pool.submit(runnable);
        }

    }


}

package com.tander.flowable.client.controller;

import com.tander.flowable.client.model.CreationProcessesStat;
import com.tander.flowable.client.service.BpmnProcessCreationService;
import com.tander.flowable.client.service.BpmnSignalService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/process")
@RequiredArgsConstructor
public class BpmProcessController {

    private final BpmnProcessCreationService processTestService;

    private final BpmnSignalService bpmnSignalService;

    @Operation
    @GetMapping("/start")
    public void start(@RequestParam int processCount, @RequestParam int itemCount) {
        processTestService.deployAndRunProcess(processCount, itemCount);
    }

    @GetMapping("/deploy")
    public void deploy() {
        processTestService.deployProcess(true);
    }

    @GetMapping("/continue")
    public void continueProcesses() {
        bpmnSignalService.continueProcesses();
    }


}

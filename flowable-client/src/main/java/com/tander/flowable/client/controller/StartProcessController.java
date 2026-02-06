package com.tander.flowable.client.controller;

import com.tander.flowable.client.service.BpmnProcessService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/process")
public class StartProcessController {

    @Autowired
    private BpmnProcessService processTestService;

    @Operation
    @GetMapping("/start")
    public void start() {
        processTestService.deployAndTestProcess();
    }

    @GetMapping("/deploy")
    public void deploy() {
        processTestService.deployProcess(true);
    }

}

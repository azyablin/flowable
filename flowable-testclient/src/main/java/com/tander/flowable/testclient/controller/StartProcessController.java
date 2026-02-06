package com.tander.flowable.testclient.controller;


import com.tander.flowable.testclient.action.ProcessTestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/process")
public class StartProcessController {

    @Autowired
    private ProcessTestService processTestService;

    @GetMapping("/start")
    public void stsrt() {
        processTestService.deployAndTestProcess();
    }

}

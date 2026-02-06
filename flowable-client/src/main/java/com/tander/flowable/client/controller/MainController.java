package com.tander.flowable.client.controller;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/")
public class MainController {

    @RequestMapping("/index")
    public String index() {
        return "Flowable Client is running!";
    }

}

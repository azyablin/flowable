package com.tander.flowable.testclient;

import org.apache.ibatis.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"com.tander", "org.flowable.engine"})
public class Main {
    public static void main(String[] args) {
        LogFactory.useStdOutLogging();
        SpringApplication.run(Main.class, args);
    }
}
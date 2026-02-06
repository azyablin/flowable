package com.tander.flowable.client;

import org.apache.ibatis.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.tander", "org.flowable.engine", "org.flowable.rest.service", "org.flowable.app.rest.conf"})
@EnableScheduling
public class Main {
    public static void main(String[] args) {
      // LogFactory.useStdOutLogging();
        SpringApplication.run(Main.class, args);
    }
}
package com.tander.flowable.client;

import com.tander.flowable.client.config.properties.AppFlowableProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.logging.LogFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = {"com.tander", "org.flowable.engine", "org.flowable.rest.service", "org.flowable.app.rest.conf"})
@EnableScheduling
@Slf4j

@EntityScan(
    basePackages = "com.tander.flowable.client.model"
)
public class Main extends SpringBootServletInitializer {

    public static void main(String[] args) {
       // LogFactory.useStdOutLogging();
        SpringApplication.run(Main.class, args);
    }

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        logger.info("flowable slient started");
        return application.sources(Main.class);
    }


}
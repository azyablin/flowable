package com.tander.flowable.client.service;

import com.tander.flowable.client.aspect.LogErrors;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

@Service
public class TransactionalService {

    @Transactional
    @LogErrors
    public void execute(Runnable runnable) {
        runnable.run();
    }

    @Async("customTaskExecutor")
    @LogErrors
    public void execute1(Runnable runnable) {
        runnable.run();
    }

    @Async(CustomTaskExecutor.NAME)
    @LogErrors
    public void executeAsync(Runnable runnable) {
        runnable.run();
    }


    @Component(CustomTaskExecutor.NAME)
    public static class CustomTaskExecutor extends ThreadPoolTaskExecutor {

        public static final String NAME = "customTaskExecutor";

    }

}

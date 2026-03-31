package com.tander.flowable.client.action;

import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class TransactionalExecutor {

    @Transactional(rollbackFor = {Exception.class })
    public void execute(Runnable runnable) {
        runnable.run();
    }

    @Transactional(rollbackFor = {Exception.class })
    public <T> T executeAndGet(Supplier<T> supplier) {
        return supplier.get();
    }
}

package com.tander.flowable.client.interceptor;

import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandConfig;
import org.flowable.common.engine.impl.interceptor.CommandExecutor;
import org.flowable.common.spring.SpringTransactionInterceptor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;


public class CustomSpringTransactionInterceptor extends SpringTransactionInterceptor {

    public CustomSpringTransactionInterceptor(PlatformTransactionManager transactionManager) {
        super(transactionManager);

    }

    @Override
    public <T> T execute(CommandConfig config, Command<T> command, CommandExecutor commandExecutor) {
        System.out.println("execute Transaction Interceptor");
        return super.execute(config, command, commandExecutor);
    }
}

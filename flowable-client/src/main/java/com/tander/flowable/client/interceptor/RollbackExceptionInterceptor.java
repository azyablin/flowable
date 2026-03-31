package com.tander.flowable.client.interceptor;

import com.tander.flowable.client.action.TransactionalExecutor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.interceptor.AbstractCommandInterceptor;
import org.flowable.common.engine.impl.interceptor.Command;
import org.flowable.common.engine.impl.interceptor.CommandConfig;
import org.flowable.common.engine.impl.interceptor.CommandExecutor;
import org.flowable.job.service.impl.cmd.ExecuteAsyncRunnableJobCmd;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.transaction.UnexpectedRollbackException;

@RequiredArgsConstructor
@Slf4j
public class RollbackExceptionInterceptor extends AbstractCommandInterceptor {

    private static ThreadLocal<Class<?>> threadLocalVariable = new ThreadLocal<>();

    private final TransactionalExecutor transactionalExecutor;

    @Override
    public <T> T execute(CommandConfig config, Command<T> command, CommandExecutor commandExecutor) {
        try {
            if (command instanceof ExecuteAsyncRunnableJobCmd) {
                return transactionalExecutor.executeAndGet(() -> getNext().execute(config, command, commandExecutor));
             /*   try {
                    return transactionalExecutor.executeAndGet(() -> getNext().execute(config, command, commandExecutor));
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                    return null;
                }*/
            }
            return getNext().execute(config, command, commandExecutor);
        } catch (UnexpectedRollbackException e) {
            log.error(e.getMessage(), e);
            throw new RuntimeException(e);
            //  return null;
            //org.springframework.transaction.UnexpectedRollbackException: Transaction silently rolled back because it has been marked as rollback-only

        }
    }

    public static void setThreadLocalVariable(Class<?> currentClass) {
        threadLocalVariable.set(currentClass);
    }


}

package com.tander.flowable.client.mybatis.interceptor;


import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.*;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

@Intercepts({
    @Signature(
        type = Executor.class,
        method = "update",
        args = {MappedStatement.class, Object.class}
    )
})
public class JobDeleteInterceptor implements Interceptor {

    private static final Logger logger = LoggerFactory.getLogger(JobDeleteInterceptor.class);

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        Object[] args = invocation.getArgs();
        MappedStatement ms = (MappedStatement) args[0];
        Object parameter = args[1];

        BoundSql boundSql = ms.getBoundSql(parameter);
        String sql = boundSql.getSql();

        // Проверяем, что это DELETE из таблицы джобов (можно уточнить по имени таблицы)
        if (sql != null && sql.toUpperCase().trim().startsWith("DELETE FROM ACT_RU_JOB")) {
            logger.info("Перехвачен DELETE джобов: {}", sql);
            logger.debug("Параметры: {}", parameter);
            // Здесь можно добавить свою логику (аудит, логирование, изменение параметров)
        }

        return invocation.proceed();
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // Можно читать настройки из application.properties
    }
}
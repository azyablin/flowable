package com.tander.flowable.client.mybatis.interceptor;


import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.InnerInterceptor;
import com.tander.flowable.client.mybatis.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.flowable.app.engine.impl.util.CommandContextUtil;
import org.flowable.engine.ManagementService;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.Map;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
public class JobInterceptor implements InnerInterceptor {


    private final JobExecutor jobExecutor;


    @Lookup
    public ManagementService managementService() {
        return null;
    }

    @Override
    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds, ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        jobExecutor.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
    }

    @Override
    public boolean willDoUpdate(Executor executor, MappedStatement ms, Object parameter) throws SQLException {
        return jobExecutor.willDoUpdate(executor, ms, parameter);
    }

}
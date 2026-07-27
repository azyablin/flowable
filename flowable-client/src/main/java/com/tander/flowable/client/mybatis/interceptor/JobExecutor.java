package com.tander.flowable.client.mybatis.interceptor;

import com.baomidou.mybatisplus.core.plugins.InterceptorIgnoreHelper;
import com.baomidou.mybatisplus.core.toolkit.PluginUtils;
import com.baomidou.mybatisplus.extension.plugins.handler.TableNameHandler;
import com.baomidou.mybatisplus.extension.plugins.inner.DynamicTableNameInnerInterceptor;
import com.tander.flowable.client.mybatis.exception.UnknownStatementException;
import com.tander.flowable.client.mybatis.mapper.JobMapper;
import lombok.RequiredArgsConstructor;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.flowable.app.engine.impl.util.CommandContextUtil;
import org.flowable.common.engine.impl.db.ListQueryParameterObject;
import org.flowable.engine.ManagementService;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.job.service.impl.persistence.entity.JobEntityImpl;
import org.springframework.beans.factory.annotation.Lookup;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.stream.Stream;

@Component
@RequiredArgsConstructor
public class JobExecutor {

    private static final String VIEW_NAME = "V_ACT_RU_JOB";

    private static final String TABLE_NAME = "ACT_RU_JOB";

    private static final String NAME_SPACE = "org.flowable.job.service.impl.persistence.entity.JobEntityImpl.";

    private static final String UPDATE_JOB_LOCKS_ID = NAME_SPACE + "updateJobLocks";

    private static final String RESET_EXPIRED_JOB_ID = NAME_SPACE + "resetExpiredJob";

    private static final String INSERT_JOB_ID = NAME_SPACE + "insertJob";

    private static final String BULK_INSERT_JOB = NAME_SPACE + "bulkInsertJob";

    private static final String UPDATE_JOB = NAME_SPACE + "updateJob";

    private static final Map<String, Boolean> IGNORED_STATEMENTS = new ConcurrentHashMap<>();

    private static final Set<String> HANDLED_STATEMENTS = Set.of(UPDATE_JOB_LOCKS_ID, RESET_EXPIRED_JOB_ID,
        INSERT_JOB_ID, BULK_INSERT_JOB, UPDATE_JOB);

    private static final DynamicTableNameInnerInterceptor DYNAMIC_TABLE_NAME_INNER_INTERCEPTOR =
        new JobDynamicTableNameInnerInterceptor(new JobTableHandler());

    @Lookup
    public ManagementService managementService() {
        return null;
    }

    public void beforeQuery(Executor executor, MappedStatement ms, Object parameter, RowBounds rowBounds,
                            ResultHandler resultHandler, BoundSql boundSql) throws SQLException {
        if (!ms.getId().startsWith(NAME_SPACE)) {
            return;
        }
        DYNAMIC_TABLE_NAME_INNER_INTERCEPTOR.beforeQuery(executor, ms, parameter, rowBounds, resultHandler, boundSql);
    }

    public boolean willDoUpdate(Executor executor, MappedStatement ms, Object parameter) {
        if (!ms.getId().startsWith(NAME_SPACE)) {
            return true;
        }
        if (UPDATE_JOB_LOCKS_ID.equals(ms.getId())) {
            executeWithContext(jobMapper -> jobMapper.insertJobLocksIfNotExist((Map<String, Object>) parameter));
            return false;
        }
        var id = ms.getId();
        if (ms.getSqlCommandType() == SqlCommandType.DELETE || HANDLED_STATEMENTS.contains(id) || IGNORED_STATEMENTS.containsKey(id)) {
            return true;
        }
        var sql = ms.getBoundSql(parameter).getSql();
        if (!sql.contains("lockOwner")) {
            IGNORED_STATEMENTS.put(id, Boolean.TRUE);
        } else {
            throw new UnknownStatementException("Unknown statement with id %s ".formatted(id));
        }
        return true;
    }

    @SuppressWarnings({"unchecked"})
    public void afterUpdate(MappedStatement ms, Object parameter) {
        var id = ms.getId();
        List<String> ids = switch (id) {
            case INSERT_JOB_ID, UPDATE_JOB -> List.of(((JobEntity) parameter).getId());
            case BULK_INSERT_JOB -> {
                List<JobEntity> list;
                if (parameter instanceof Map<?, ?> map) {
                    list = (List<JobEntity>) map.get("list");
                } else {
                    list = (List<JobEntity>) parameter;
                }
                yield list.stream()
                    .map(JobEntity::getId).toList();
            }
            case RESET_EXPIRED_JOB_ID -> {
                Map<String, Object> params;
                if (parameter instanceof ListQueryParameterObject queryParameterObject) {
                    params = (Map<String, Object>) queryParameterObject.getParameter();
                } else {
                    params = (Map<String, Object>) parameter;
                }
                yield List.of(params.get("id").toString());
            }
            default -> Collections.emptyList();
        };
        if (!ids.isEmpty()) {
            executeWithContext(jobMapper -> {
                if (ms.getSqlCommandType() == SqlCommandType.INSERT) {
                    jobMapper.insertJobLocksIfLocked(ids);
                } else {
                    var count = jobMapper.deleteJobLocksIfNotLocked(ids);
                    if (count < ids.size()) {
                        jobMapper.insertJobLocksIfLocked(ids);
                    }
                }
            });
        }
    }

    private void executeWithContext(Consumer<JobMapper> jobMapperConsumer) {
       /* managementService().executeCommand(commandContext -> {
            JobMapper mapper = CommandContextUtil.getDbSqlSession()
                .getCustomMapper(JobMapper.class);
            jobMapperConsumer.accept(mapper);
            return null;
        });*/
        JobMapper mapper = CommandContextUtil.getDbSqlSession()
            .getCustomMapper(JobMapper.class);
        jobMapperConsumer.accept(mapper);
    }

    public static class JobTableHandler implements TableNameHandler {

        @Override
        public String dynamicTableName(String sql, String tableName) {
            return TABLE_NAME.equals(tableName) ? VIEW_NAME : tableName;
        }
    }

    public static class JobDynamicTableNameInnerInterceptor extends DynamicTableNameInnerInterceptor {

        private static final Map<String, String> SQL_CACHE = new ConcurrentHashMap<>();

        public JobDynamicTableNameInnerInterceptor(TableNameHandler tableNameHandler) {
            super(tableNameHandler);
        }

        @Override
        public String changeTable(String sql) {
            return SQL_CACHE.computeIfAbsent(sql, super::changeTable);
        }

    }


}

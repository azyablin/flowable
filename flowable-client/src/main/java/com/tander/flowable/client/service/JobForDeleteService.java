package com.tander.flowable.client.service;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.flowable.common.engine.impl.Page;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.hibernate.Session;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class JobForDeleteService {

    private static final int MAX_IN_LENGTH = 1000;
    private static final String ACT_RU_JOB_FOR_DELETE_TABLE = "ACT_RU_JOB_FOR_DELETE";
    private static final String ACT_RU_JOB_TABLE = "ACT_RU_JOB";
    private static final String INSERT_SQL = """
            insert into %s select id_ from  %s where execution_id_ = :execution_id
        """.formatted(ACT_RU_JOB_FOR_DELETE_TABLE, ACT_RU_JOB_TABLE);
    private static final String SELECT_SQL = """
        select id_ from %s
        """.formatted(ACT_RU_JOB_FOR_DELETE_TABLE);
    private static final String UPDATE_BATCH_GUID_SQL = """
        update %s set batch_guid = :batch_guid
        """.formatted(ACT_RU_JOB_FOR_DELETE_TABLE);
    private static final String SELECT_FROM_ACT_RU_JOB_FOR_DELETE = """
        select id_ from ACT_RU_JOB_FOR_DELETE for update skip locked
        """;

    private final EntityManager entityManager;
    private final Session session;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteJobsByExecutionId(String executionId) {
        entityManager.createNativeQuery(INSERT_SQL)
            .setParameter("execution_id", executionId)
            .executeUpdate();
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deleteJobs() {
        List<String> ids = new ArrayList<>();
        try (Stream stream =entityManager.createNativeQuery(SELECT_FROM_ACT_RU_JOB_FOR_DELETE).getResultStream()) {
            stream
                .map(o -> "'".concat(o.toString()).concat("'"))
                .forEach(id -> {
                    ids.add(id.toString());
                    if (ids.size() >= MAX_IN_LENGTH) {
                        deleteByIds(ids);
                    }
                });
        }
        deleteByIds(ids);
    }

    private void deleteByIds(List<String> ids) {
        String deleteFromJobSql = """
            delete from %s where id_ in (%s)
            """;
        if (!ids.isEmpty()) {
            var inList = String.join(",", ids);
            entityManager.createNativeQuery(deleteFromJobSql.formatted(ACT_RU_JOB_TABLE, inList))
                .executeUpdate();
            entityManager.createNativeQuery(deleteFromJobSql.formatted(ACT_RU_JOB_FOR_DELETE_TABLE, inList))
                .executeUpdate();
            ids.clear();
        }
    }

}

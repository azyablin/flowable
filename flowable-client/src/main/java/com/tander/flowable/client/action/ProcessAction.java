package com.tander.flowable.client.action;


import com.tander.flowable.client.model.BpmEntity;
import com.tander.flowable.client.repository.BpmEntityRepository;
import com.tander.flowable.client.service.BpmExecutionService;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.spring.SpringTransactionInterceptor;
import org.flowable.engine.ManagementService;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.hibernate.PropertyValueException;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentHashMap;

@Component("processAction")
@RequiredArgsConstructor
@Slf4j
public class ProcessAction implements JavaDelegate {


    private ConcurrentHashMap<String, Exception> EXECUTION_EXCEPTION = new ConcurrentHashMap<>();

    private final EntityManager entityManager;

    private final BpmExecutionService bpmExecutionService;

    private final BpmEntityRepository bpmEntityRepository;

    private final TransactionalExecutor transactionalExecutor;

    @Override
    @Transactional
    public void execute(DelegateExecution execution) {
       /* try {
            transactionalExecutor.execute(() ->
            {
                BpmEntity bpmEntity = new BpmEntity();
                bpmEntity.setId("qqq");
                entityManager.merge(bpmEntity);

            });
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }*/
        BpmEntity bpmEntity = new BpmEntity();
        bpmEntity.setId("qqq");
        entityManager.merge(bpmEntity);
        log.info("Подпроцесс запущен: Connection {} , Thread {}, Task {}", entityManager.hashCode(), Thread.currentThread().getId(), execution.getCurrentActivityId());
  }


}
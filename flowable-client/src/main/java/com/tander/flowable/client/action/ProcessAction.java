package com.tander.flowable.client.action;


import com.tander.flowable.client.model.BpmEntity;
import com.tander.flowable.client.repository.BpmEntityRepository;
import com.tander.flowable.client.service.BpmExecutionService;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.spring.SpringTransactionInterceptor;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.hibernate.Session;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Connection;
import java.sql.SQLException;

@Component("processAction")
@RequiredArgsConstructor
@Slf4j
public class ProcessAction implements JavaDelegate {



    private final EntityManager entityManager;

    private final BpmExecutionService bpmExecutionService;

    private final BpmEntityRepository bpmEntityRepository;

    @Override
    @Transactional(noRollbackFor = {SQLException.class, DataAccessException.class})
    public void execute(DelegateExecution execution) {

        BpmEntity bpmEntity = new BpmEntity();
        bpmEntity.setId("qqq");
        try {
            bpmEntityRepository.save(bpmEntity);
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        log.info("Подпроцесс запущен: Connection {} , Thread {}, Task {}", entityManager.hashCode(), Thread.currentThread().getId(), execution.getCurrentActivityId());
    }
}
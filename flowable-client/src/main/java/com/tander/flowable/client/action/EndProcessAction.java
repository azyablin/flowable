package com.tander.flowable.client.action;


import com.tander.flowable.client.model.ActionErrorInfo;
import com.tander.flowable.client.model.BpmEntity;
import com.tander.flowable.client.repository.BpmEntityRepository;
import com.tander.flowable.client.service.BpmExecutionService;
import javax.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.flowable.engine.delegate.DelegateExecution;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Component("endProcessAction")
@RequiredArgsConstructor
@Slf4j
public class EndProcessAction extends BaseAction {

    private final EntityManager entityManager;

    private final BpmExecutionService bpmExecutionService;

    private final BpmEntityRepository bpmEntityRepository;

    private final TransactionalExecutor transactionalExecutor;

    @Override
    public void internalExecute(DelegateExecution execution) {
        BpmEntity bpmEntity = new BpmEntity();
        bpmEntity.setId("qqq");
        bpmEntity.setCode("eee");
        entityManager.merge(bpmEntity);
        bpmEntity = new BpmEntity();
        bpmEntity.setId("qqq1");
        bpmEntity.setCode("eee");
        entityManager.merge(bpmEntity);
        log.info("Подпроцесс запущен: Connection {} , Thread {}, Task {}", entityManager.hashCode(), Thread.currentThread().getId(), execution.getCurrentActivityId());

    }

    @Override
    public void handleActionErrorInfo(DelegateExecution execution, ActionErrorInfo actionErrorInfo) {
        log.info("Подпроцесс запущен: Connection {} , Thread {}, Task {}", entityManager.hashCode(), Thread.currentThread().getId(), execution.getCurrentActivityId());
    }


}
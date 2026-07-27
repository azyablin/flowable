package com.tander.flowable.client.manager;

import org.flowable.common.engine.impl.Page;
import org.flowable.job.service.JobServiceConfiguration;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.job.service.impl.persistence.entity.JobEntityManagerImpl;
import org.flowable.job.service.impl.persistence.entity.data.JobDataManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Date;
import java.util.List;


public class CustomJobEntityManagerImpl extends JobEntityManagerImpl {


    private final TransactionTemplate transactionTemplate;

    public CustomJobEntityManagerImpl(PlatformTransactionManager transactionManager, JobServiceConfiguration jobServiceConfiguration, JobDataManager jobDataManager) {
        super(jobServiceConfiguration, jobDataManager);
        this.transactionTemplate = new TransactionTemplate(transactionManager);
    }

    @Override
    public List<JobEntity> findJobsToExecuteAndLockInBulk(List<String> enabledCategories, Page page, String lockOwner, Date lockExpirationTime) {
        transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        return transactionTemplate.execute(status ->
            super.findJobsToExecuteAndLockInBulk(enabledCategories, page, lockOwner, lockExpirationTime));
    }

}

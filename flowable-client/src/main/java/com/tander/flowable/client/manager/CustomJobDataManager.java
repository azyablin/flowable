package com.tander.flowable.client.manager;

import com.tander.flowable.client.service.JobForDeleteService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.flowable.common.engine.impl.Page;
import org.flowable.job.service.JobServiceConfiguration;
import org.flowable.job.service.impl.persistence.entity.JobEntity;
import org.flowable.job.service.impl.persistence.entity.JobEntityManagerImpl;
import org.flowable.job.service.impl.persistence.entity.data.impl.MybatisJobDataManager;
import org.springframework.stereotype.Component;

import java.util.List;

//@Component
@Slf4j
public class CustomJobDataManager extends MybatisJobDataManager {

    private EntityManager entityManager;

    private JobForDeleteService jobForDeleteService;

    public CustomJobDataManager(JobServiceConfiguration jobServiceConfiguration, EntityManager entityManager, JobForDeleteService jobForDeleteService) {
        super(jobServiceConfiguration);
        this.entityManager = entityManager;
        jobServiceConfiguration.setJobDataManager(this);
        jobServiceConfiguration.setJobEntityManager(new JobEntityManagerImpl(jobServiceConfiguration, this));
        this.jobForDeleteService = jobForDeleteService;
    }


    @Override
    @SuppressWarnings("unchecked")
    public List<JobEntity> findJobsToExecute(List<String> enabledCategories, Page page) {
        jobForDeleteService.deleteJobs();
        return super.findJobsToExecute(enabledCategories, page);
    }

    @Override
    public void deleteJobsByExecutionId(String executionId) {
        jobForDeleteService.deleteJobsByExecutionId(executionId);
    }


}

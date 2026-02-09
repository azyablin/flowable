package com.tander.flowable.client.service;

import com.tander.flowable.client.model.BpmExecution;
import com.tander.flowable.client.model.BpmExecutionInfo;
import com.tander.flowable.client.model.BpmProcess;
import com.tander.flowable.client.repository.BpmExecutionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
public class BpmExecutionService {

    private final BpmExecutionRepository bpmExecutionRepository;

    @Transactional
    public BpmExecutionInfo processIds(Consumer<String> consumer) {
        var ids = bpmExecutionRepository.findWithSkipLockedNative()
            .limit(100)
            .map(BpmExecution::getId)
            .toList();
        ids.forEach(consumer);
        bpmExecutionRepository.deleteAllByIdInBatch(ids);
        return new BpmExecutionInfo(ids.size(), bpmExecutionRepository.count()) ;
    }

    public void addExecution(String executionId) {
        bpmExecutionRepository.save(new BpmExecution().setId(executionId));
    }

}

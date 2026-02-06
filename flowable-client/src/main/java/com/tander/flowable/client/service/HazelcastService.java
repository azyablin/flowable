package com.tander.flowable.client.service;

import com.hazelcast.collection.IList;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.spring.cache.HazelcastCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Queue;

@Service
@RequiredArgsConstructor
@Slf4j
public class HazelcastService {

    private static final String EXECUTION_MAP_NAME = "product";

    private final HazelcastInstance hazelcastInstance;

    public void addExecution(String executionId) {
        var queue = getExecutionQueue();
        log.info("add to execution list id {}. count {}. thread id {}", executionId, queue.size(), Thread.currentThread().getId());
        queue.add(executionId);
    }



    public Queue<String> getExecutionQueue() {
        return hazelcastInstance.getQueue(EXECUTION_MAP_NAME);
    }



}

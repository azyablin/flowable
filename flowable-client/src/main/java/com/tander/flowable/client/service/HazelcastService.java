package com.tander.flowable.client.service;

import com.hazelcast.collection.IList;
import com.hazelcast.collection.IQueue;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.spring.cache.HazelcastCache;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Queue;

@Service
@RequiredArgsConstructor
@Slf4j
public class HazelcastService {


    @Value("${server.port}")
    private int serverPort;


    private static final String EXECUTION_MAP_NAME = "product";

    private static final String LOCK_MAP_NAME = "flowable_lock";

    private final HazelcastInstance hazelcastInstance;

    public void addExecution(String executionId) {
        var queue = getExecutionQueue();
        log.info("add to execution list id {}. count {}. thread id {}", executionId, queue.size(), Thread.currentThread().getId());
        queue.add(executionId);
    }

    public Queue<String> getExecutionQueue() {
        return hazelcastInstance.getQueue(EXECUTION_MAP_NAME);
    }

    public boolean tryLock(String lockName) {
        var map = hazelcastInstance.getMap(LOCK_MAP_NAME);
        map.putIfAbsent(lockName, serverPort);
        return map.tryLock(LOCK_MAP_NAME);
    }

    public void unlock(String lockName) {
        var map = hazelcastInstance.getMap(LOCK_MAP_NAME);
        map.unlock(lockName);

    }

    public long getLockCount() {
        var stats = hazelcastInstance.getMap(LOCK_MAP_NAME).getLocalMapStats();
        return stats.getLockedEntryCount();
    }

}

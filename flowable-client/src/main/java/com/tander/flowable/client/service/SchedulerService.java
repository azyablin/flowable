package com.tander.flowable.client.service;

import com.hazelcast.collection.IQueue;
import com.hazelcast.core.DistributedObject;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.map.IMap;
import com.hazelcast.topic.ITopic;
import com.tander.flowable.client.action.AsyncResponseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class SchedulerService {
    private final HazelcastInstance hazelcastInstance;

    private final HazelcastService hazelcastService;

    private final AsyncResponseService asyncResponseService;

    private final AtomicBoolean started = new AtomicBoolean();

    @Value("${server.port:8095}")
    private int serverPort;

    @Scheduled(fixedDelay = 10000)
    public void runAsyncProcesses() {
        inspectAllMaps();
      if (serverPort != 8096) {
            return;
        }


        var executionQueue = hazelcastService.getExecutionQueue();
        if (executionQueue.size() >= /*ProcessService.TOTAL_COUNT*/ 0) {
            started.set(true);
            asyncResponseService.setItemCount(executionQueue.size());
            Stream.generate(executionQueue::poll)
                .limit(executionQueue.size())
                .takeWhile(Objects::nonNull)
               // .parallel()
                .forEach(s -> {
                    asyncResponseService.startAsyncProcessing(s, executionQueue.size());
                });
        }

        if (started.get() && asyncResponseService.getItemCount() <= 0) {
            started.set(false);
            log.info("**************FINISH ALL PROCESSES");
        }

    }

    public void inspectAllMaps() {
        // Получить все существующие Distributed Objects
        Collection<DistributedObject> distributedObjects =
            hazelcastInstance.getDistributedObjects();

        for (DistributedObject obj : distributedObjects) {
            log.info("Distributed Object: {}", obj.getName());
            log.info("  Service: {}", obj.getServiceName());

            if (obj instanceof IMap) {
                var m = ((IMap<?, ?>) obj);
                System.out.println(m.size());
            } /*else if (obj instanceof IQueue) {
                inspectQueue((IQueue<?>) obj);
            } else if (obj instanceof ITopic) {
                inspectTopic((ITopic<?>) obj);
            }*/
        }
    }
}

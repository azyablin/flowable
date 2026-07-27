package com.tander.flowable.client.service;

import com.tander.flowable.client.model.BpmExecution;
import com.tander.flowable.client.model.BpmExecutionInfo;
import com.tander.flowable.client.model.BpmProcess;
import com.tander.flowable.client.repository.BpmExecutionRepository;
import com.tander.flowable.client.repository.BpmProcessRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Slf4j
public class BpmProcessService {


    private final BpmProcessRepository bpmProcessRepository;

    private final BpmExecutionRepository bpmExecutionRepository;

    @Transactional
    public void fillProcessTable(int processCount) {
        var processes = IntStream.range(0, processCount)
            .boxed()
            .map(integer -> new BpmProcess())
            .toList();
        bpmProcessRepository.saveAll(processes);
    }

    @Transactional
    public  List<BpmProcess>  getBatch() {
        var processes = bpmProcessRepository.findWithSkipLockedNative()
            .limit(100)
            .toList();
        processes.forEach(bpmProcess -> bpmProcess.setProgress(1));
        bpmProcessRepository.saveAll(processes);
        return processes;
    }


    public void finishProcess(String processId) {
        if (bpmProcessRepository.deleteByProcessId(processId) == 0) {
            log.info("Процесс {} для завершения не найден {}", processId);
        }
    }

    public long count() {
        return bpmProcessRepository.count();
    }

    public BpmProcess save(BpmProcess bpmProcess) {
       return bpmProcessRepository.save(bpmProcess);
    }

    public long countByProcessIdNotNull() {
        return bpmProcessRepository.count();
    }


    @Transactional
    public void clearProcesses() {
        bpmProcessRepository.deleteAll();
        bpmExecutionRepository.deleteAll();

    }

}

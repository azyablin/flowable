package com.tander.flowable.client.service;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TransactionalService {

    @Transactional
    public void execute(Runnable runnable) {
        runnable.run();
    }

}

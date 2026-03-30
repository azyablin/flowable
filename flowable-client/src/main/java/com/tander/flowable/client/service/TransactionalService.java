package com.tander.flowable.client.service;

import javax.transaction.Transactional;
import org.springframework.stereotype.Service;

@Service
public class TransactionalService {

    @Transactional
    public void execute(Runnable runnable) {
        runnable.run();
    }

}

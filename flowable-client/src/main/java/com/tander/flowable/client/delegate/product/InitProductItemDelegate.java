package com.tander.flowable.client.delegate.product;

import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("initProductItemDelegate")
public class InitProductItemDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        System.out.println("initProductItemDelegate");

    }
}

package com.tander.flowable.client.action;

import com.tander.flowable.client.model.Product;
import com.tander.flowable.client.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.delegate.DelegateExecution;
import org.flowable.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

import static com.tander.flowable.client.model.Product.VAR_NAME;

@Component("processProduct")
@RequiredArgsConstructor
public class ProcessProduct implements JavaDelegate {

    private final ProductRepository productRepository;

    @Override
    public void execute(DelegateExecution execution) {
        /*Product product = (Product) execution.getVariableLocal(VAR_NAME);
        product.setStatus(1).setUpdateThreadId(Thread.currentThread().getId());
        productRepository.save(product);*/
    }
}

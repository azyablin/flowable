package com.tander.flowable.client.repository;

import com.tander.flowable.client.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, String>  {
}

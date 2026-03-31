package com.tander.flowable.client.repository;

import com.tander.flowable.client.model.BpmEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BpmEntityRepository extends JpaRepository<BpmEntity, String>  {

}

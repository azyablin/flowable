package com.tander.flowable.client.repository;

import com.tander.flowable.client.model.BpmExecution;
import com.tander.flowable.client.model.BpmProcess;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public interface BpmProcessRepository extends JpaRepository<BpmProcess, String> {


    @Query(value = """
 
            SELECT be1_0.* 
            FROM bpm_process be1_0 
            WHERE process_id is null  
            ORDER BY be1_0.id
                     FOR UPDATE SKIP LOCKED
 
        """,
        nativeQuery = true)
    Stream<BpmProcess> findWithSkipLockedNative();

    @Query(value = """
        select name_ from FLOWABLE.ACT_GE_PROPERTY WHERE  name_ = :property FOR UPDATE SKIP LOCKED
        """,
        nativeQuery = true)
    Optional<String> lockProperty(@Param("property")  String property);

    long deleteByProcessId(String processId);

    long countByProcessIdNotNull();

}

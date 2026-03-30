package com.tander.flowable.client.repository;



import com.tander.flowable.client.model.BpmExecution;
import com.tander.flowable.client.util.QueryHintsUtil;
import javax.persistence.LockModeType;
import javax.persistence.QueryHint;
import org.hibernate.cfg.AvailableSettings;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.stream.Stream;

public interface BpmExecutionRepository extends JpaRepository<BpmExecution, String> {


    @Query(value = """
 
            SELECT be1_0.* 
            FROM bpm_execution be1_0 
                     FOR UPDATE SKIP LOCKED
 
        """,
        nativeQuery = true)
    Stream<BpmExecution> findWithSkipLockedNative();


}

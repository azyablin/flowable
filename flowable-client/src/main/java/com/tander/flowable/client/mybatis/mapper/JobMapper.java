package com.tander.flowable.client.mybatis.mapper;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

@Mapper
public interface JobMapper {

    @Insert("""
            <script>
            INSERT INTO ACT_RU_JOB_LOCK (ID_, LOCK_OWNER_, LOCK_EXP_TIME_)
            SELECT j.ID_, #{params.lockOwner, jdbcType=NVARCHAR}, #{params.lockExpirationTime, jdbcType=TIMESTAMP}
            FROM ACT_RU_JOB j
            WHERE j.ID_ IN
            <foreach item="job" index="index" collection="params.jobs" open="(" separator="," close=")">
                #{job.id, jdbcType=NVARCHAR}
            </foreach>
            AND NOT EXISTS (
                SELECT 1 FROM ACT_RU_JOB_LOCK l WHERE l.ID_ = j.ID_
            )
            </script>
            """)
    int insertJobLocksIfNotExist(@Param("params") Map<String, Object> params);

    @Delete("""
        <script>
            DELETE FROM ACT_RU_JOB_LOCK WHERE ID_ IN (
                SELECT ID_ FROM ACT_RU_JOB j WHERE  
                j.LOCK_OWNER_ IS NULL AND
                j.ID_ IN
                <foreach item="id" index="index" collection="ids" open="(" separator="," close=")">
                    #{id, jdbcType=NVARCHAR}
                </foreach>
            )
        </script>
        """)
    int deleteJobLocksIfNotLocked(@Param("ids") List<String> ids);


    @Insert("""
            <script>
            INSERT INTO ACT_RU_JOB_LOCK (ID_, LOCK_OWNER_, LOCK_EXP_TIME_)
            SELECT j.ID_, j.LOCK_OWNER_, j.LOCK_EXP_TIME_
            FROM ACT_RU_JOB j
            WHERE 
            j.LOCK_OWNER_ IS NOT NULL AND
            j.ID_ IN
            <foreach item="id" index="index" collection="ids" open="(" separator="," close=")">
                #{id, jdbcType=NVARCHAR}
            </foreach>
            AND NOT EXISTS (
                SELECT 1 FROM ACT_RU_JOB_LOCK l WHERE l.ID_ = j.ID_
            )
            </script>
            """)
    int insertJobLocksIfLocked(@Param("ids") List<String> ids);


}

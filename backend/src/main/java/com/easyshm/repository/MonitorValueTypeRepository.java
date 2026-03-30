package com.easyshm.repository;

import com.easyshm.entity.MonitorValueType;
import com.easyshm.repository.projection.MonitorValueTypeWithIndexView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MonitorValueTypeRepository extends JpaRepository<MonitorValueType, Long> {

    @Query("""
            select
              m.id as id,
              m.monitorIndexId as monitorIndexId,
              m.name as name,
              m.unit as unit,
              m.createdAt as createdAt,
              m.updatedAt as updatedAt,
              i.name as monitorIndexName,
              i.type as monitorIndexType,
              i.code as monitorIndexCode,
              i.departmentId as monitorIndexDepartmentId
            from MonitorValueType m
            join MonitorIndex i on m.monitorIndexId = i.id
            where (:monitorIndexId is null or m.monitorIndexId = :monitorIndexId)
            """)
    List<MonitorValueTypeWithIndexView> findAllWithMonitorIndex(@Param("monitorIndexId") Long monitorIndexId);

    List<MonitorValueType> findByMonitorIndexId(Long monitorIndexId);

    void deleteByMonitorIndexId(Long monitorIndexId);
}

package com.easyshm.repository;

import com.easyshm.entity.MonitorValueType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MonitorValueTypeRepository extends JpaRepository<MonitorValueType, Long> {

    Page<MonitorValueType> findByMonitorIndexId(Long monitorIndexId, Pageable pageable);

    List<MonitorValueType> findByMonitorIndexId(Long monitorIndexId);

    void deleteByMonitorIndexId(Long monitorIndexId);
}

package com.easyshm.repository;

import com.easyshm.entity.MonitorIndex;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitorIndexRepository extends JpaRepository<MonitorIndex, Long> {

    Page<MonitorIndex> findByNameContaining(String name, Pageable pageable);

    Page<MonitorIndex> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<MonitorIndex> findByType(Integer type, Pageable pageable);

    Page<MonitorIndex> findByNameContainingAndDepartmentId(String name, Long departmentId, Pageable pageable);

    Page<MonitorIndex> findByNameContainingAndType(String name, Integer type, Pageable pageable);

    Page<MonitorIndex> findByDepartmentIdAndType(Long departmentId, Integer type, Pageable pageable);

    Page<MonitorIndex> findByNameContainingAndDepartmentIdAndType(String name, Long departmentId, Integer type, Pageable pageable);
}

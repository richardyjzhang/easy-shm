package com.easyshm.repository;

import com.easyshm.entity.Structure;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StructureRepository extends JpaRepository<Structure, Long> {

    Page<Structure> findByNameContaining(String name, Pageable pageable);

    Page<Structure> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<Structure> findByNameContainingAndDepartmentId(String name, Long departmentId, Pageable pageable);
}

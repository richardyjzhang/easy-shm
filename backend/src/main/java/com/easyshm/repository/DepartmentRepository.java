package com.easyshm.repository;

import com.easyshm.entity.Department;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DepartmentRepository extends JpaRepository<Department, Long> {

    Page<Department> findByNameContaining(String name, Pageable pageable);
}

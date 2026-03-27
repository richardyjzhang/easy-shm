package com.easyshm.service;

import com.easyshm.entity.Department;
import org.springframework.data.domain.Page;

public interface DepartmentService {

    Page<Department> list(String name, int page, int size);

    Department getById(Long id);

    Department create(Department department);

    Department update(Long id, Department department);

    void delete(Long id);
}

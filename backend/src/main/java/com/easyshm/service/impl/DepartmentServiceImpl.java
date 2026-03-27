package com.easyshm.service.impl;

import com.easyshm.entity.Department;
import com.easyshm.repository.DepartmentRepository;
import com.easyshm.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

@Service
public class DepartmentServiceImpl implements DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public Page<Department> list(String name, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        if (name != null && !name.isBlank()) {
            return departmentRepository.findByNameContaining(name.trim(), pageable);
        }
        return departmentRepository.findAll(pageable);
    }

    @Override
    public Department getById(Long id) {
        return departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("机构不存在（id=" + id + "）"));
    }

    @Override
    public Department create(Department department) {
        department.setId(null);
        return departmentRepository.save(department);
    }

    @Override
    public Department update(Long id, Department department) {
        Department existing = getById(id);
        existing.setName(department.getName());
        existing.setContact(department.getContact());
        existing.setPhone(department.getPhone());
        existing.setAddress(department.getAddress());
        existing.setDescription(department.getDescription());
        return departmentRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!departmentRepository.existsById(id)) {
            throw new RuntimeException("机构不存在（id=" + id + "）");
        }
        departmentRepository.deleteById(id);
    }
}

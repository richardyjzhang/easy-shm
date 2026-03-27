package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.entity.Department;
import com.easyshm.service.DepartmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    @GetMapping
    public ApiResponse<Page<Department>> list(
            @RequestParam(required = false) String name,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(departmentService.list(name, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Department> getById(@PathVariable Long id) {
        return ApiResponse.ok(departmentService.getById(id));
    }

    @PostMapping
    public ApiResponse<Department> create(@RequestBody Department department) {
        return ApiResponse.ok(departmentService.create(department));
    }

    @PutMapping("/{id}")
    public ApiResponse<Department> update(@PathVariable Long id, @RequestBody Department department) {
        return ApiResponse.ok(departmentService.update(id, department));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        departmentService.delete(id);
        return ApiResponse.ok();
    }
}

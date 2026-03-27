package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.entity.Structure;
import com.easyshm.service.StructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/structures")
public class StructureController {

    @Autowired
    private StructureService structureService;

    @GetMapping
    public ApiResponse<Page<Structure>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(structureService.list(name, departmentId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<Structure> getById(@PathVariable Long id) {
        return ApiResponse.ok(structureService.getById(id));
    }

    @PostMapping
    public ApiResponse<Structure> create(@RequestBody Structure structure) {
        return ApiResponse.ok(structureService.create(structure));
    }

    @PutMapping("/{id}")
    public ApiResponse<Structure> update(@PathVariable Long id, @RequestBody Structure structure) {
        return ApiResponse.ok(structureService.update(id, structure));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        structureService.delete(id);
        return ApiResponse.ok();
    }
}

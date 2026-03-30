package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.dto.MonitorIndexWithValueTypesDTO;
import com.easyshm.entity.MonitorIndex;
import com.easyshm.service.MonitorIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor-indexes")
public class MonitorIndexController {

    @Autowired
    private MonitorIndexService monitorIndexService;

    @GetMapping
    public ApiResponse<Page<MonitorIndex>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer type,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(monitorIndexService.list(name, departmentId, type, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<MonitorIndex> getById(@PathVariable Long id) {
        return ApiResponse.ok(monitorIndexService.getById(id));
    }

    @GetMapping("/with-value-types")
    public ApiResponse<List<MonitorIndexWithValueTypesDTO>> listAllWithValueTypes(
            @RequestParam(required = false) Long departmentId) {
        return ApiResponse.ok(monitorIndexService.listAllWithValueTypes(departmentId));
    }

    @PostMapping
    public ApiResponse<MonitorIndex> create(@RequestBody MonitorIndex monitorIndex) {
        return ApiResponse.ok(monitorIndexService.create(monitorIndex));
    }

    @PutMapping("/{id}")
    public ApiResponse<MonitorIndex> update(@PathVariable Long id, @RequestBody MonitorIndex monitorIndex) {
        return ApiResponse.ok(monitorIndexService.update(id, monitorIndex));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        monitorIndexService.delete(id);
        return ApiResponse.ok();
    }
}

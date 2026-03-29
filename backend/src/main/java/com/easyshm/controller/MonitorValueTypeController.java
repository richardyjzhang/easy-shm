package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.entity.MonitorValueType;
import com.easyshm.service.MonitorValueTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitor-value-types")
public class MonitorValueTypeController {

    @Autowired
    private MonitorValueTypeService monitorValueTypeService;

    @GetMapping
    public ApiResponse<Page<MonitorValueType>> list(
            @RequestParam(required = false) Long monitorIndexId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        if (monitorIndexId == null) {
            throw new RuntimeException("缺少 monitorIndexId 参数");
        }
        return ApiResponse.ok(monitorValueTypeService.listByMonitorIndexId(monitorIndexId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<MonitorValueType> getById(@PathVariable Long id) {
        return ApiResponse.ok(monitorValueTypeService.getById(id));
    }

    @PostMapping
    public ApiResponse<MonitorValueType> create(@RequestBody MonitorValueType monitorValueType) {
        return ApiResponse.ok(monitorValueTypeService.create(monitorValueType));
    }

    @PutMapping("/{id}")
    public ApiResponse<MonitorValueType> update(@PathVariable Long id, @RequestBody MonitorValueType monitorValueType) {
        return ApiResponse.ok(monitorValueTypeService.update(id, monitorValueType));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        monitorValueTypeService.delete(id);
        return ApiResponse.ok();
    }
}

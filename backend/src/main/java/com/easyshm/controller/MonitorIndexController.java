package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.entity.MonitorIndex;
import com.easyshm.service.MonitorIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor-indexes")
public class MonitorIndexController {

    @Autowired
    private MonitorIndexService monitorIndexService;

    @GetMapping
    public ApiResponse<List<MonitorIndex>> list(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Integer type) {
        return ApiResponse.ok(monitorIndexService.list(name, departmentId, type));
    }

    @GetMapping("/{id}")
    public ApiResponse<MonitorIndex> getById(@PathVariable Long id) {
        return ApiResponse.ok(monitorIndexService.getById(id));
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

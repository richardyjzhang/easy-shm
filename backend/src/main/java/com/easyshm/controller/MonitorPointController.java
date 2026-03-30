package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.entity.MonitorPoint;
import com.easyshm.service.MonitorPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor-points")
public class MonitorPointController {

    @Autowired
    private MonitorPointService monitorPointService;

    @GetMapping
    public ApiResponse<List<MonitorPoint>> list(@RequestParam Long structureId) {
        return ApiResponse.ok(monitorPointService.listByStructureId(structureId));
    }

    @GetMapping("/{id}")
    public ApiResponse<MonitorPoint> getById(@PathVariable Long id) {
        return ApiResponse.ok(monitorPointService.getById(id));
    }

    @PostMapping
    public ApiResponse<MonitorPoint> create(@RequestBody MonitorPoint monitorPoint) {
        return ApiResponse.ok(monitorPointService.create(monitorPoint));
    }

    @PutMapping("/{id}")
    public ApiResponse<MonitorPoint> update(@PathVariable Long id, @RequestBody MonitorPoint monitorPoint) {
        return ApiResponse.ok(monitorPointService.update(id, monitorPoint));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        monitorPointService.delete(id);
        return ApiResponse.ok();
    }
}

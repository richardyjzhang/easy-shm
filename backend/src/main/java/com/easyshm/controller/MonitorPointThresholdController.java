package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.entity.MonitorPointThreshold;
import com.easyshm.service.MonitorPointThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/monitor-point-thresholds")
public class MonitorPointThresholdController {

    @Autowired
    private MonitorPointThresholdService thresholdService;

    @GetMapping
    public ApiResponse<List<MonitorPointThreshold>> listByPointIds(@RequestParam List<Long> pointIds) {
        return ApiResponse.ok(thresholdService.listByPointIds(pointIds));
    }

    @GetMapping("/by-point/{pointId}")
    public ApiResponse<MonitorPointThreshold> getByPointId(@PathVariable Long pointId) {
        return ApiResponse.ok(thresholdService.getByPointId(pointId));
    }

    @PostMapping
    public ApiResponse<MonitorPointThreshold> save(@RequestBody MonitorPointThreshold threshold) {
        return ApiResponse.ok(thresholdService.save(threshold));
    }

    @DeleteMapping("/by-point/{pointId}")
    public ApiResponse<Void> deleteByPointId(@PathVariable Long pointId) {
        thresholdService.deleteByPointId(pointId);
        return ApiResponse.ok();
    }
}

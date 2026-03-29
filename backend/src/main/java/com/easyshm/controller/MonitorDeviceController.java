package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.entity.MonitorDevice;
import com.easyshm.service.MonitorDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/monitor-devices")
public class MonitorDeviceController {

    @Autowired
    private MonitorDeviceService monitorDeviceService;

    @GetMapping
    public ApiResponse<Page<MonitorDevice>> list(
            @RequestParam(required = false) String sn,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) Long deviceModelId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(monitorDeviceService.list(sn, departmentId, deviceModelId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<MonitorDevice> getById(@PathVariable Long id) {
        return ApiResponse.ok(monitorDeviceService.getById(id));
    }

    @PostMapping
    public ApiResponse<MonitorDevice> create(@RequestBody MonitorDevice monitorDevice) {
        return ApiResponse.ok(monitorDeviceService.create(monitorDevice));
    }

    @PutMapping("/{id}")
    public ApiResponse<MonitorDevice> update(@PathVariable Long id, @RequestBody MonitorDevice monitorDevice) {
        return ApiResponse.ok(monitorDeviceService.update(id, monitorDevice));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        monitorDeviceService.delete(id);
        return ApiResponse.ok();
    }
}

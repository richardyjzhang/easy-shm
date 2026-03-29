package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.entity.DeviceModel;
import com.easyshm.service.DeviceModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/device-models")
public class DeviceModelController {

    @Autowired
    private DeviceModelService deviceModelService;

    @GetMapping
    public ApiResponse<Page<DeviceModel>> list(
            @RequestParam(required = false) String manufacturer,
            @RequestParam(required = false) String deviceType,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) Long departmentId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ApiResponse.ok(deviceModelService.list(manufacturer, deviceType, model, departmentId, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<DeviceModel> getById(@PathVariable Long id) {
        return ApiResponse.ok(deviceModelService.getById(id));
    }

    @PostMapping
    public ApiResponse<DeviceModel> create(@RequestBody DeviceModel deviceModel) {
        return ApiResponse.ok(deviceModelService.create(deviceModel));
    }

    @PutMapping("/{id}")
    public ApiResponse<DeviceModel> update(@PathVariable Long id, @RequestBody DeviceModel deviceModel) {
        return ApiResponse.ok(deviceModelService.update(id, deviceModel));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        deviceModelService.delete(id);
        return ApiResponse.ok();
    }
}

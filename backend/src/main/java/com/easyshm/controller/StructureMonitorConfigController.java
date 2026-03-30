package com.easyshm.controller;

import com.easyshm.dto.ApiResponse;
import com.easyshm.entity.StructureMonitorConfig;
import com.easyshm.repository.StructureMonitorConfigRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/structure-monitor-configs")
public class StructureMonitorConfigController {

    @Autowired
    private StructureMonitorConfigRepository repository;

    @GetMapping
    public ApiResponse<List<StructureMonitorConfig>> listByStructureId(@RequestParam Long structureId) {
        return ApiResponse.ok(repository.findByStructureId(structureId));
    }

    @PostMapping
    @Transactional
    public ApiResponse<Void> saveBatch(@RequestParam Long structureId, @RequestBody List<Long> valueTypeIds) {
        repository.findByStructureId(structureId).forEach(config -> {
            repository.delete(config);
        });
        for (Long valueTypeId : valueTypeIds) {
            StructureMonitorConfig config = new StructureMonitorConfig();
            config.setStructureId(structureId);
            config.setValueTypeId(valueTypeId);
            repository.save(config);
        }
        return ApiResponse.ok();
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        repository.deleteById(id);
        return ApiResponse.ok();
    }
}

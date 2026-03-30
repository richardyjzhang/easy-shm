package com.easyshm.service.impl;

import com.easyshm.entity.MonitorPointThreshold;
import com.easyshm.repository.MonitorPointRepository;
import com.easyshm.repository.MonitorPointThresholdRepository;
import com.easyshm.service.MonitorPointThresholdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MonitorPointThresholdServiceImpl implements MonitorPointThresholdService {

    @Autowired
    private MonitorPointThresholdRepository thresholdRepository;

    @Autowired
    private MonitorPointRepository monitorPointRepository;

    @Override
    public List<MonitorPointThreshold> listByPointIds(List<Long> pointIds) {
        if (pointIds == null || pointIds.isEmpty()) {
            return List.of();
        }
        return thresholdRepository.findByPointIdIn(pointIds);
    }

    @Override
    public MonitorPointThreshold getByPointId(Long pointId) {
        return thresholdRepository.findByPointId(pointId).orElse(null);
    }

    @Override
    public MonitorPointThreshold save(MonitorPointThreshold incoming) {
        if (incoming.getPointId() == null) {
            throw new RuntimeException("测点ID不能为空");
        }
        if (!monitorPointRepository.existsById(incoming.getPointId())) {
            throw new RuntimeException("测点不存在（id=" + incoming.getPointId() + "）");
        }
        if (incoming.getEnabled() == null) {
            incoming.setEnabled(1);
        }
        MonitorPointThreshold existing = thresholdRepository.findByPointId(incoming.getPointId()).orElse(null);
        if (existing != null) {
            existing.setEnabled(incoming.getEnabled());
            existing.setBlueLower(incoming.getBlueLower());
            existing.setBlueUpper(incoming.getBlueUpper());
            existing.setYellowLower(incoming.getYellowLower());
            existing.setYellowUpper(incoming.getYellowUpper());
            existing.setRedLower(incoming.getRedLower());
            existing.setRedUpper(incoming.getRedUpper());
            return thresholdRepository.save(existing);
        }
        incoming.setId(null);
        return thresholdRepository.save(incoming);
    }

    @Override
    public void deleteByPointId(Long pointId) {
        thresholdRepository.deleteByPointId(pointId);
    }
}

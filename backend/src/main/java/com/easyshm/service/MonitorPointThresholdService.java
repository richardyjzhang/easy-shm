package com.easyshm.service;

import com.easyshm.entity.MonitorPointThreshold;

import java.util.List;

public interface MonitorPointThresholdService {

    List<MonitorPointThreshold> listByPointIds(List<Long> pointIds);

    MonitorPointThreshold getByPointId(Long pointId);

    MonitorPointThreshold save(MonitorPointThreshold threshold);

    void deleteByPointId(Long pointId);
}

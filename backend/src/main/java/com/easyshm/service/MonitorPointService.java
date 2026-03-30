package com.easyshm.service;

import com.easyshm.entity.MonitorPoint;

import java.util.List;

public interface MonitorPointService {

    List<MonitorPoint> listByStructureId(Long structureId);

    MonitorPoint getById(Long id);

    MonitorPoint create(MonitorPoint monitorPoint);

    MonitorPoint update(Long id, MonitorPoint monitorPoint);

    void delete(Long id);
}

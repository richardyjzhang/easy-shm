package com.easyshm.service;

import com.easyshm.entity.MonitorValueType;
import com.easyshm.repository.projection.MonitorValueTypeWithIndexView;

import java.util.List;

public interface MonitorValueTypeService {

    List<MonitorValueTypeWithIndexView> listWithMonitorIndex(Long monitorIndexId);

    MonitorValueType getById(Long id);

    MonitorValueType create(MonitorValueType monitorValueType);

    MonitorValueType update(Long id, MonitorValueType monitorValueType);

    void delete(Long id);
}

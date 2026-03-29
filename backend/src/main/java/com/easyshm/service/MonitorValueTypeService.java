package com.easyshm.service;

import com.easyshm.entity.MonitorValueType;
import org.springframework.data.domain.Page;

public interface MonitorValueTypeService {

    Page<MonitorValueType> listByMonitorIndexId(Long monitorIndexId, int page, int size);

    MonitorValueType getById(Long id);

    MonitorValueType create(MonitorValueType monitorValueType);

    MonitorValueType update(Long id, MonitorValueType monitorValueType);

    void delete(Long id);
}

package com.easyshm.service;

import com.easyshm.entity.MonitorIndex;

import java.util.List;

public interface MonitorIndexService {

    List<MonitorIndex> list(String name, Long departmentId, Integer type);

    MonitorIndex getById(Long id);

    MonitorIndex create(MonitorIndex monitorIndex);

    MonitorIndex update(Long id, MonitorIndex monitorIndex);

    void delete(Long id);
}

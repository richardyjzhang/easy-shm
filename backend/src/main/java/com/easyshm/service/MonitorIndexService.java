package com.easyshm.service;

import com.easyshm.entity.MonitorIndex;
import org.springframework.data.domain.Page;

public interface MonitorIndexService {

    Page<MonitorIndex> list(String name, Long departmentId, Integer type, int page, int size);

    MonitorIndex getById(Long id);

    MonitorIndex create(MonitorIndex monitorIndex);

    MonitorIndex update(Long id, MonitorIndex monitorIndex);

    void delete(Long id);
}

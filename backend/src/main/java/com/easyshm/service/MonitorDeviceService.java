package com.easyshm.service;

import com.easyshm.entity.MonitorDevice;
import org.springframework.data.domain.Page;

public interface MonitorDeviceService {

    Page<MonitorDevice> list(String sn, Long departmentId, Long deviceModelId, int page, int size);

    MonitorDevice getById(Long id);

    MonitorDevice create(MonitorDevice monitorDevice);

    MonitorDevice update(Long id, MonitorDevice monitorDevice);

    void delete(Long id);
}

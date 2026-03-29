package com.easyshm.service;

import com.easyshm.entity.DeviceModel;
import org.springframework.data.domain.Page;

public interface DeviceModelService {

    Page<DeviceModel> list(String manufacturer, String deviceType, String model, Long departmentId, int page, int size);

    DeviceModel getById(Long id);

    DeviceModel create(DeviceModel deviceModel);

    DeviceModel update(Long id, DeviceModel deviceModel);

    void delete(Long id);
}

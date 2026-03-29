package com.easyshm.repository;

import com.easyshm.entity.DeviceModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceModelRepository extends JpaRepository<DeviceModel, Long> {

    Page<DeviceModel> findByManufacturerContaining(String manufacturer, Pageable pageable);

    Page<DeviceModel> findByDeviceTypeContaining(String deviceType, Pageable pageable);

    Page<DeviceModel> findByModelContaining(String model, Pageable pageable);

    Page<DeviceModel> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<DeviceModel> findByDeviceTypeContainingAndDepartmentId(String deviceType, Long departmentId, Pageable pageable);

    Page<DeviceModel> findByModelContainingAndDepartmentId(String model, Long departmentId, Pageable pageable);

    Page<DeviceModel> findByDeviceTypeContainingAndModelContaining(String deviceType, String model, Pageable pageable);

    Page<DeviceModel> findByDeviceTypeContainingAndModelContainingAndDepartmentId(String deviceType, String model, Long departmentId, Pageable pageable);
}

package com.easyshm.repository;

import com.easyshm.entity.MonitorDevice;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MonitorDeviceRepository extends JpaRepository<MonitorDevice, Long> {

    Page<MonitorDevice> findBySnContaining(String sn, Pageable pageable);

    Page<MonitorDevice> findByDepartmentId(Long departmentId, Pageable pageable);

    Page<MonitorDevice> findByDeviceModelId(Long deviceModelId, Pageable pageable);

    Page<MonitorDevice> findByDepartmentIdAndDeviceModelId(Long departmentId, Long deviceModelId, Pageable pageable);

    Page<MonitorDevice> findByDepartmentIdAndSnContaining(Long departmentId, String sn, Pageable pageable);

    Page<MonitorDevice> findByDeviceModelIdAndSnContaining(Long deviceModelId, String sn, Pageable pageable);

    Page<MonitorDevice> findByDepartmentIdAndDeviceModelIdAndSnContaining(Long departmentId, Long deviceModelId, String sn, Pageable pageable);
}

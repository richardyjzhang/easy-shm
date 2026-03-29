package com.easyshm.service.impl;

import com.easyshm.entity.MonitorDevice;
import com.easyshm.repository.DepartmentRepository;
import com.easyshm.repository.DeviceModelRepository;
import com.easyshm.repository.MonitorDeviceRepository;
import com.easyshm.service.MonitorDeviceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MonitorDeviceServiceImpl implements MonitorDeviceService {

    @Autowired
    private MonitorDeviceRepository monitorDeviceRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Autowired
    private DeviceModelRepository deviceModelRepository;

    @Override
    public Page<MonitorDevice> list(String sn, Long departmentId, Long deviceModelId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        boolean hasSn = StringUtils.hasText(sn);
        boolean hasDept = departmentId != null;
        boolean hasModel = deviceModelId != null;

        if (hasDept && hasModel && hasSn) {
            return monitorDeviceRepository.findByDepartmentIdAndDeviceModelIdAndSnContaining(
                    departmentId, deviceModelId, sn.trim(), pageable);
        }
        if (hasDept && hasSn) {
            return monitorDeviceRepository.findByDepartmentIdAndSnContaining(
                    departmentId, sn.trim(), pageable);
        }
        if (hasModel && hasSn) {
            return monitorDeviceRepository.findByDeviceModelIdAndSnContaining(
                    deviceModelId, sn.trim(), pageable);
        }
        if (hasDept && hasModel) {
            return monitorDeviceRepository.findByDepartmentIdAndDeviceModelId(
                    departmentId, deviceModelId, pageable);
        }
        if (hasSn) {
            return monitorDeviceRepository.findBySnContaining(sn.trim(), pageable);
        }
        if (hasModel) {
            return monitorDeviceRepository.findByDeviceModelId(deviceModelId, pageable);
        }
        if (hasDept) {
            return monitorDeviceRepository.findByDepartmentId(departmentId, pageable);
        }
        return monitorDeviceRepository.findAll(pageable);
    }

    @Override
    public MonitorDevice getById(Long id) {
        return monitorDeviceRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("监测设备不存在（id=" + id + "）"));
    }

    @Override
    public MonitorDevice create(MonitorDevice monitorDevice) {
        validate(monitorDevice);
        monitorDevice.setId(null);
        return monitorDeviceRepository.save(monitorDevice);
    }

    @Override
    public MonitorDevice update(Long id, MonitorDevice incoming) {
        validate(incoming);
        MonitorDevice existing = getById(id);
        existing.setDepartmentId(incoming.getDepartmentId());
        existing.setDeviceModelId(incoming.getDeviceModelId());
        existing.setSn(incoming.getSn());
        existing.setProductionDate(incoming.getProductionDate());
        existing.setRemark(incoming.getRemark());
        return monitorDeviceRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!monitorDeviceRepository.existsById(id)) {
            throw new RuntimeException("监测设备不存在（id=" + id + "）");
        }
        monitorDeviceRepository.deleteById(id);
    }

    private void validate(MonitorDevice d) {
        if (!StringUtils.hasText(d.getSn())) {
            throw new RuntimeException("序列号不能为空");
        }
        d.setSn(d.getSn().trim());
        if (d.getDepartmentId() == null) {
            throw new RuntimeException("所属机构不能为空");
        }
        if (!departmentRepository.existsById(d.getDepartmentId())) {
            throw new RuntimeException("机构不存在（id=" + d.getDepartmentId() + "）");
        }
        if (d.getDeviceModelId() == null) {
            throw new RuntimeException("设备型号不能为空");
        }
        if (!deviceModelRepository.existsById(d.getDeviceModelId())) {
            throw new RuntimeException("设备型号不存在（id=" + d.getDeviceModelId() + "）");
        }
    }
}

package com.easyshm.service.impl;

import com.easyshm.entity.DeviceModel;
import com.easyshm.repository.DepartmentRepository;
import com.easyshm.repository.DeviceModelRepository;
import com.easyshm.service.DeviceModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class DeviceModelServiceImpl implements DeviceModelService {

    @Autowired
    private DeviceModelRepository deviceModelRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public Page<DeviceModel> list(String manufacturer, String deviceType, String model, Long departmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        boolean hasManufacturer = StringUtils.hasText(manufacturer);
        boolean hasDeviceType = StringUtils.hasText(deviceType);
        boolean hasModel = StringUtils.hasText(model);
        boolean hasDept = departmentId != null;

        // 为了简化，使用 Specification 或示例查询；这里简单处理：优先按厂商模糊查询
        if (hasManufacturer) {
            return deviceModelRepository.findByManufacturerContaining(manufacturer.trim(), pageable);
        }
        if (hasDeviceType && hasModel && hasDept) {
            return deviceModelRepository.findByDeviceTypeContainingAndModelContainingAndDepartmentId(
                    deviceType.trim(), model.trim(), departmentId, pageable);
        }
        if (hasDeviceType && hasModel) {
            return deviceModelRepository.findByDeviceTypeContainingAndModelContaining(
                    deviceType.trim(), model.trim(), pageable);
        }
        if (hasDeviceType && hasDept) {
            return deviceModelRepository.findByDeviceTypeContainingAndDepartmentId(
                    deviceType.trim(), departmentId, pageable);
        }
        if (hasModel && hasDept) {
            return deviceModelRepository.findByModelContainingAndDepartmentId(
                    model.trim(), departmentId, pageable);
        }
        if (hasDeviceType) {
            return deviceModelRepository.findByDeviceTypeContaining(deviceType.trim(), pageable);
        }
        if (hasModel) {
            return deviceModelRepository.findByModelContaining(model.trim(), pageable);
        }
        if (hasDept) {
            return deviceModelRepository.findByDepartmentId(departmentId, pageable);
        }
        return deviceModelRepository.findAll(pageable);
    }

    @Override
    public DeviceModel getById(Long id) {
        return deviceModelRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("设备型号不存在（id=" + id + "）"));
    }

    @Override
    public DeviceModel create(DeviceModel deviceModel) {
        validate(deviceModel);
        deviceModel.setId(null);
        return deviceModelRepository.save(deviceModel);
    }

    @Override
    public DeviceModel update(Long id, DeviceModel incoming) {
        validate(incoming);
        DeviceModel existing = getById(id);
        existing.setManufacturer(incoming.getManufacturer());
        existing.setDeviceType(incoming.getDeviceType());
        existing.setModel(incoming.getModel());
        existing.setRange(incoming.getRange());
        existing.setPrincipleType(incoming.getPrincipleType());
        existing.setSensitivity(incoming.getSensitivity());
        existing.setAccuracy(incoming.getAccuracy());
        existing.setDepartmentId(incoming.getDepartmentId());
        existing.setRemark(incoming.getRemark());
        return deviceModelRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!deviceModelRepository.existsById(id)) {
            throw new RuntimeException("设备型号不存在（id=" + id + "）");
        }
        deviceModelRepository.deleteById(id);
    }

    private void validate(DeviceModel d) {
        if (!StringUtils.hasText(d.getManufacturer())) {
            throw new RuntimeException("设备厂商不能为空");
        }
        if (!StringUtils.hasText(d.getDeviceType())) {
            throw new RuntimeException("设备类型不能为空");
        }
        if (!StringUtils.hasText(d.getModel())) {
            throw new RuntimeException("设备型号不能为空");
        }
        d.setManufacturer(d.getManufacturer().trim());
        d.setDeviceType(d.getDeviceType().trim());
        d.setModel(d.getModel().trim());
        if (d.getDepartmentId() == null) {
            throw new RuntimeException("所属机构不能为空");
        }
        if (!departmentRepository.existsById(d.getDepartmentId())) {
            throw new RuntimeException("机构不存在（id=" + d.getDepartmentId() + "）");
        }
    }
}

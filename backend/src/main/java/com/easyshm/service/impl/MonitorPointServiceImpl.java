package com.easyshm.service.impl;

import com.easyshm.entity.MonitorPoint;
import com.easyshm.repository.MonitorDeviceRepository;
import com.easyshm.repository.MonitorPointRepository;
import com.easyshm.repository.StructureMonitorConfigRepository;
import com.easyshm.service.MonitorPointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

@Service
public class MonitorPointServiceImpl implements MonitorPointService {

    @Autowired
    private MonitorPointRepository monitorPointRepository;

    @Autowired
    private MonitorDeviceRepository monitorDeviceRepository;

    @Autowired
    private StructureMonitorConfigRepository structureMonitorConfigRepository;

    @Override
    public List<MonitorPoint> listByStructureId(Long structureId) {
        if (structureId == null) {
            throw new RuntimeException("结构物ID不能为空");
        }
        return monitorPointRepository.findByStructureId(structureId);
    }

    @Override
    public MonitorPoint getById(Long id) {
        return monitorPointRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("测点不存在（id=" + id + "）"));
    }

    @Override
    public MonitorPoint create(MonitorPoint monitorPoint) {
        validate(monitorPoint);
        if (monitorPointRepository.existsByStructureIdAndCode(monitorPoint.getStructureId(), monitorPoint.getCode())) {
            throw new RuntimeException("该结构物下测点编号已存在：" + monitorPoint.getCode());
        }
        monitorPoint.setId(null);
        return monitorPointRepository.save(monitorPoint);
    }

    @Override
    public MonitorPoint update(Long id, MonitorPoint incoming) {
        validate(incoming);
        MonitorPoint existing = getById(id);
        if (monitorPointRepository.existsByStructureIdAndCodeAndIdNot(incoming.getStructureId(), incoming.getCode(), id)) {
            throw new RuntimeException("该结构物下测点编号已存在：" + incoming.getCode());
        }
        existing.setStructureId(incoming.getStructureId());
        existing.setValueTypeId(incoming.getValueTypeId());
        existing.setCode(incoming.getCode());
        existing.setLocation(incoming.getLocation());
        existing.setDeviceId(incoming.getDeviceId());
        existing.setChannel(incoming.getChannel());
        return monitorPointRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!monitorPointRepository.existsById(id)) {
            throw new RuntimeException("测点不存在（id=" + id + "）");
        }
        monitorPointRepository.deleteById(id);
    }

    private void validate(MonitorPoint p) {
        if (p.getStructureId() == null) {
            throw new RuntimeException("所属结构物不能为空");
        }
        if (p.getValueTypeId() == null) {
            throw new RuntimeException("监测内容不能为空");
        }
        if (!StringUtils.hasText(p.getCode())) {
            throw new RuntimeException("测点编号不能为空");
        }
        p.setCode(p.getCode().trim());
        if (p.getDeviceId() == null) {
            throw new RuntimeException("选用设备不能为空");
        }
        if (!monitorDeviceRepository.existsById(p.getDeviceId())) {
            throw new RuntimeException("设备不存在（id=" + p.getDeviceId() + "）");
        }
        if (StringUtils.hasText(p.getLocation())) {
            p.setLocation(p.getLocation().trim());
        }
        if (StringUtils.hasText(p.getChannel())) {
            p.setChannel(p.getChannel().trim());
        }
    }
}

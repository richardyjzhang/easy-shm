package com.easyshm.service.impl;

import com.easyshm.entity.MonitorValueType;
import com.easyshm.repository.MonitorIndexRepository;
import com.easyshm.repository.MonitorValueTypeRepository;
import com.easyshm.service.MonitorValueTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MonitorValueTypeServiceImpl implements MonitorValueTypeService {

    @Autowired
    private MonitorValueTypeRepository monitorValueTypeRepository;

    @Autowired
    private MonitorIndexRepository monitorIndexRepository;

    @Override
    public Page<MonitorValueType> listByMonitorIndexId(Long monitorIndexId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return monitorValueTypeRepository.findByMonitorIndexId(monitorIndexId, pageable);
    }

    @Override
    public MonitorValueType getById(Long id) {
        return monitorValueTypeRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("监测内容不存在（id=" + id + "）"));
    }

    @Override
    public MonitorValueType create(MonitorValueType monitorValueType) {
        validate(monitorValueType);
        monitorValueType.setId(null);
        return monitorValueTypeRepository.save(monitorValueType);
    }

    @Override
    public MonitorValueType update(Long id, MonitorValueType incoming) {
        validate(incoming);
        MonitorValueType existing = getById(id);
        existing.setName(incoming.getName());
        existing.setUnit(incoming.getUnit());
        existing.setMonitorIndexId(incoming.getMonitorIndexId());
        return monitorValueTypeRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!monitorValueTypeRepository.existsById(id)) {
            throw new RuntimeException("监测内容不存在（id=" + id + "）");
        }
        monitorValueTypeRepository.deleteById(id);
    }

    private void validate(MonitorValueType m) {
        if (!StringUtils.hasText(m.getName())) {
            throw new RuntimeException("监测内容名称不能为空");
        }
        if (m.getMonitorIndexId() == null) {
            throw new RuntimeException("所属监测项不能为空");
        }
        if (!monitorIndexRepository.existsById(m.getMonitorIndexId())) {
            throw new RuntimeException("监测项不存在（id=" + m.getMonitorIndexId() + "）");
        }
        m.setName(m.getName().trim());
        if (StringUtils.hasText(m.getUnit())) {
            m.setUnit(m.getUnit().trim());
        }
    }
}

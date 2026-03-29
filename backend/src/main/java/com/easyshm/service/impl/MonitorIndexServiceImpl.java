package com.easyshm.service.impl;

import com.easyshm.entity.MonitorIndex;
import com.easyshm.repository.DepartmentRepository;
import com.easyshm.repository.MonitorIndexRepository;
import com.easyshm.repository.MonitorValueTypeRepository;
import com.easyshm.service.MonitorIndexService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
public class MonitorIndexServiceImpl implements MonitorIndexService {

    private static final Set<Integer> ALLOWED_TYPES = Set.of(1, 2, 3, 4);

    @Autowired
    private MonitorIndexRepository monitorIndexRepository;

    @Autowired
    private MonitorValueTypeRepository monitorValueTypeRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public Page<MonitorIndex> list(String name, Long departmentId, Integer type, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        boolean hasName = StringUtils.hasText(name);
        boolean hasDept = departmentId != null;
        boolean hasType = type != null;

        if (hasName && hasDept && hasType) {
            return monitorIndexRepository.findByNameContainingAndDepartmentIdAndType(name.trim(), departmentId, type, pageable);
        }
        if (hasName && hasDept) {
            return monitorIndexRepository.findByNameContainingAndDepartmentId(name.trim(), departmentId, pageable);
        }
        if (hasName && hasType) {
            return monitorIndexRepository.findByNameContainingAndType(name.trim(), type, pageable);
        }
        if (hasDept && hasType) {
            return monitorIndexRepository.findByDepartmentIdAndType(departmentId, type, pageable);
        }
        if (hasName) {
            return monitorIndexRepository.findByNameContaining(name.trim(), pageable);
        }
        if (hasDept) {
            return monitorIndexRepository.findByDepartmentId(departmentId, pageable);
        }
        if (hasType) {
            return monitorIndexRepository.findByType(type, pageable);
        }
        return monitorIndexRepository.findAll(pageable);
    }

    @Override
    public MonitorIndex getById(Long id) {
        return monitorIndexRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("监测项不存在（id=" + id + "）"));
    }

    @Override
    public MonitorIndex create(MonitorIndex monitorIndex) {
        validate(monitorIndex);
        monitorIndex.setId(null);
        return monitorIndexRepository.save(monitorIndex);
    }

    @Override
    public MonitorIndex update(Long id, MonitorIndex incoming) {
        validate(incoming);
        MonitorIndex existing = getById(id);
        existing.setName(incoming.getName());
        existing.setType(incoming.getType());
        existing.setCode(incoming.getCode());
        existing.setDepartmentId(incoming.getDepartmentId());
        return monitorIndexRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!monitorIndexRepository.existsById(id)) {
            throw new RuntimeException("监测项不存在（id=" + id + "）");
        }
        // 级联删除其下所有监测内容
        monitorValueTypeRepository.deleteByMonitorIndexId(id);
        monitorIndexRepository.deleteById(id);
    }

    private void validate(MonitorIndex m) {
        if (!StringUtils.hasText(m.getName())) {
            throw new RuntimeException("监测项名称不能为空");
        }
        if (m.getType() == null) {
            throw new RuntimeException("监测项类型不能为空");
        }
        if (!ALLOWED_TYPES.contains(m.getType())) {
            throw new RuntimeException("监测项类型无效，应为：1=环境, 2=作用, 3=结构响应, 4=结构变化");
        }
        if (m.getDepartmentId() == null) {
            throw new RuntimeException("所属机构不能为空");
        }
        if (!departmentRepository.existsById(m.getDepartmentId())) {
            throw new RuntimeException("机构不存在（id=" + m.getDepartmentId() + "）");
        }
        m.setName(m.getName().trim());
        if (StringUtils.hasText(m.getCode())) {
            m.setCode(m.getCode().trim());
        }
    }
}

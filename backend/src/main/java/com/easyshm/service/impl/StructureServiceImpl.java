package com.easyshm.service.impl;

import com.easyshm.entity.Structure;
import com.easyshm.repository.DepartmentRepository;
import com.easyshm.repository.StructureRepository;
import com.easyshm.service.StructureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Set;

@Service
public class StructureServiceImpl implements StructureService {

    private static final Set<String> ALLOWED_TYPES = Set.of("民建", "工建", "桥梁", "隧道", "边坡", "其他");

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    @Override
    public Page<Structure> list(String name, Long departmentId, int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        boolean hasName = StringUtils.hasText(name);
        boolean hasDept = departmentId != null;
        if (hasName && hasDept) {
            return structureRepository.findByNameContainingAndDepartmentId(name.trim(), departmentId, pageable);
        }
        if (hasName) {
            return structureRepository.findByNameContaining(name.trim(), pageable);
        }
        if (hasDept) {
            return structureRepository.findByDepartmentId(departmentId, pageable);
        }
        return structureRepository.findAll(pageable);
    }

    @Override
    public Structure getById(Long id) {
        return structureRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("结构不存在（id=" + id + "）"));
    }

    @Override
    public Structure create(Structure structure) {
        validate(structure);
        structure.setId(null);
        return structureRepository.save(structure);
    }

    @Override
    public Structure update(Long id, Structure incoming) {
        validate(incoming);
        Structure existing = getById(id);
        existing.setName(incoming.getName());
        existing.setType(incoming.getType());
        existing.setDepartmentId(incoming.getDepartmentId());
        existing.setLongitude(incoming.getLongitude());
        existing.setLatitude(incoming.getLatitude());
        existing.setRemark(incoming.getRemark());
        return structureRepository.save(existing);
    }

    @Override
    public void delete(Long id) {
        if (!structureRepository.existsById(id)) {
            throw new RuntimeException("结构不存在（id=" + id + "）");
        }
        structureRepository.deleteById(id);
    }

    private void validate(Structure s) {
        if (!StringUtils.hasText(s.getName())) {
            throw new RuntimeException("结构名称不能为空");
        }
        if (!StringUtils.hasText(s.getType())) {
            throw new RuntimeException("结构类型不能为空");
        }
        String type = s.getType().trim();
        if (!ALLOWED_TYPES.contains(type)) {
            throw new RuntimeException("结构类型无效，应为：民建、工建、桥梁、隧道、边坡、其他");
        }
        s.setType(type);
        s.setName(s.getName().trim());
        if (s.getDepartmentId() == null) {
            throw new RuntimeException("所属机构不能为空");
        }
        if (!departmentRepository.existsById(s.getDepartmentId())) {
            throw new RuntimeException("机构不存在（id=" + s.getDepartmentId() + "）");
        }
        Double lng = s.getLongitude();
        Double lat = s.getLatitude();
        boolean hasLng = lng != null;
        boolean hasLat = lat != null;
        if (hasLng != hasLat) {
            throw new RuntimeException("经度与纬度需同时填写或同时留空");
        }
        if (hasLng) {
            if (lng < -180 || lng > 180 || lat < -90 || lat > 90) {
                throw new RuntimeException("经纬度数值超出有效范围");
            }
        }
    }
}

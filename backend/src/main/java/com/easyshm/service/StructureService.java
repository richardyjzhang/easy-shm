package com.easyshm.service;

import com.easyshm.entity.Structure;
import org.springframework.data.domain.Page;

public interface StructureService {

    Page<Structure> list(String name, Long departmentId, int page, int size);

    Structure getById(Long id);

    Structure create(Structure structure);

    Structure update(Long id, Structure structure);

    void delete(Long id);
}

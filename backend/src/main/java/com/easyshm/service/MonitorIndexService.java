package com.easyshm.service;

import com.easyshm.dto.MonitorIndexWithValueTypesDTO;
import com.easyshm.entity.MonitorIndex;
import org.springframework.data.domain.Page;

import java.util.List;

public interface MonitorIndexService {

    Page<MonitorIndex> list(String name, Long departmentId, Integer type, int page, int size);

    List<MonitorIndexWithValueTypesDTO> listAllWithValueTypes(Long departmentId);

    MonitorIndex getById(Long id);

    MonitorIndex create(MonitorIndex monitorIndex);

    MonitorIndex update(Long id, MonitorIndex monitorIndex);

    void delete(Long id);
}

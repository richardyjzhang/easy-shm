package com.easyshm.dto;

import com.easyshm.entity.MonitorIndex;
import com.easyshm.entity.MonitorValueType;
import lombok.Data;

import java.util.List;

@Data
public class MonitorIndexWithValueTypesDTO {

    private Long id;
    private String name;
    private String code;
    private Integer type;
    private Long departmentId;
    private List<MonitorValueType> valueTypes;

    public static MonitorIndexWithValueTypesDTO from(MonitorIndex index, List<MonitorValueType> valueTypes) {
        MonitorIndexWithValueTypesDTO dto = new MonitorIndexWithValueTypesDTO();
        dto.setId(index.getId());
        dto.setName(index.getName());
        dto.setCode(index.getCode());
        dto.setType(index.getType());
        dto.setDepartmentId(index.getDepartmentId());
        dto.setValueTypes(valueTypes);
        return dto;
    }
}

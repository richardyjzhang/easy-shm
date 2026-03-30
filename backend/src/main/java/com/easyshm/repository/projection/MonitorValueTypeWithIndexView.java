package com.easyshm.repository.projection;

import java.time.LocalDateTime;

public interface MonitorValueTypeWithIndexView {
    Long getId();
    Long getMonitorIndexId();
    String getName();
    String getUnit();
    LocalDateTime getCreatedAt();
    LocalDateTime getUpdatedAt();

    String getMonitorIndexName();
    Integer getMonitorIndexType();
    String getMonitorIndexCode();
    Long getMonitorIndexDepartmentId();
}

package com.easyshm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "monitor_point")
public class MonitorPoint {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "structure_id", nullable = false)
    private Long structureId;

    @Column(name = "value_type_id", nullable = false)
    private Long valueTypeId;

    @Column(nullable = false, length = 50)
    private String code;

    @Column(length = 255)
    private String location;

    @Column(name = "device_id", nullable = false)
    private Long deviceId;

    @Column(length = 50)
    private String channel;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}

package com.easyshm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "monitor_point_threshold")
public class MonitorPointThreshold {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "point_id", nullable = false)
    private Long pointId;

    /** 1=启用, 0=停用 */
    @Column(nullable = false)
    private Integer enabled;

    @Column(name = "blue_lower")
    private Double blueLower;

    @Column(name = "blue_upper")
    private Double blueUpper;

    @Column(name = "yellow_lower")
    private Double yellowLower;

    @Column(name = "yellow_upper")
    private Double yellowUpper;

    @Column(name = "red_lower")
    private Double redLower;

    @Column(name = "red_upper")
    private Double redUpper;

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

package com.easyshm.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "device_model")
public class DeviceModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "department_id", nullable = false)
    private Long departmentId;

    /** 设备厂商 */
    @Column(name = "manufacturer", nullable = false, length = 100)
    private String manufacturer;

    /** 设备类型：传感器、采集仪、数据传输设备等 */
    @Column(name = "device_type", nullable = false, length = 50)
    private String deviceType;

    /** 设备型号 */
    @Column(nullable = false, length = 100)
    private String model;

    /** 量程 */
    @Column(name = "`range`", length = 100)
    private String range;

    /** 原理类型：振弦式、压阻式、光纤式、电容式等 */
    @Column(name = "principle_type", length = 50)
    private String principleType;

    /** 灵敏度 */
    @Column(length = 50)
    private String sensitivity;

    /** 精度 */
    @Column(length = 50)
    private String accuracy;

    /** 备注 */
    @Column(length = 500)
    private String remark;

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

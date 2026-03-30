-- 创建数据库
CREATE DATABASE IF NOT EXISTS `easy_shm`
    DEFAULT CHARACTER SET utf8mb4
    DEFAULT COLLATE utf8mb4_general_ci;

USE `easy_shm`;

-- 组织机构
CREATE TABLE `department` (
  `id`          BIGINT       NOT NULL AUTO_INCREMENT,
  `name`        VARCHAR(100) NOT NULL COMMENT '机构名称',
  `contact`     VARCHAR(50)  DEFAULT NULL COMMENT '联系人',
  `phone`       VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  `address`     VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `description` VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='组织机构';

-- 系统用户
CREATE TABLE `sys_user` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `login_name`     VARCHAR(64)  NOT NULL COMMENT '登录名',
  `password_hash`  VARCHAR(255) NOT NULL COMMENT 'BCrypt哈希（前端已预哈希）',
  `department_id`  BIGINT       NOT NULL COMMENT '所属机构',
  `role`           TINYINT      NOT NULL DEFAULT 3 COMMENT '角色: 1系统管理员 2机构管理员 3机构用户',
  `phone`          VARCHAR(20)  DEFAULT NULL COMMENT '联系电话',
  `address`        VARCHAR(255) DEFAULT NULL COMMENT '地址',
  `remark`         VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_login_name` (`login_name`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='系统用户';

-- 监测结构
CREATE TABLE `structure` (
  `id`             BIGINT        NOT NULL AUTO_INCREMENT,
  `name`           VARCHAR(100)  NOT NULL COMMENT '结构名称',
  `type`           VARCHAR(32)   NOT NULL COMMENT '类型: 民建、工建、桥梁、隧道、边坡、其他',
  `department_id`  BIGINT        NOT NULL COMMENT '所属机构',
  `longitude`      DOUBLE        DEFAULT NULL COMMENT '经度',
  `latitude`       DOUBLE        DEFAULT NULL COMMENT '纬度',
  `remark`         VARCHAR(500)  DEFAULT NULL COMMENT '备注',
  `created_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测结构';

-- 监测项
CREATE TABLE `monitor_index` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT,
  `department_id`  BIGINT       NOT NULL COMMENT '所属机构',
  `type`           TINYINT      NOT NULL COMMENT '类型: 1环境, 2作用, 3结构响应, 4结构变化',
  `name`           VARCHAR(100) NOT NULL COMMENT '名称',
  `code`           VARCHAR(50)  DEFAULT NULL COMMENT '编码',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测项';

-- 监测内容
CREATE TABLE `monitor_value_type` (
  `id`               BIGINT       NOT NULL AUTO_INCREMENT,
  `monitor_index_id` BIGINT       NOT NULL COMMENT '所属监测项',
  `name`             VARCHAR(100) NOT NULL COMMENT '名称',
  `unit`             VARCHAR(50)  DEFAULT NULL COMMENT '单位',
  `created_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`       DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测内容';

-- 设备信息（型号规格管理）
CREATE TABLE `device_model` (
  `id`             BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `department_id`  BIGINT       NOT NULL COMMENT '所属机构',
  `manufacturer`   VARCHAR(100) NOT NULL COMMENT '设备厂商',
  `device_type`    VARCHAR(50)  NOT NULL COMMENT '设备类型：传感器、采集仪、数据传输设备等',
  `model`          VARCHAR(100) NOT NULL COMMENT '设备型号',
  `range`          VARCHAR(100) DEFAULT NULL COMMENT '量程',
  `principle_type` VARCHAR(50)  DEFAULT NULL COMMENT '原理类型：振弦式、压阻式、光纤式、电容式等',
  `sensitivity`    VARCHAR(50)  DEFAULT NULL COMMENT '灵敏度',
  `accuracy`       VARCHAR(50)  DEFAULT NULL COMMENT '精度',
  `remark`         VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='设备型号规格';

-- 监测设备（设备实例管理）
CREATE TABLE `monitor_device` (
  `id`              BIGINT       NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `department_id`   BIGINT       NOT NULL COMMENT '所属机构',
  `device_model_id` BIGINT       NOT NULL COMMENT '设备型号',
  `sn`              VARCHAR(100) NOT NULL COMMENT '序列号',
  `production_date` DATE         DEFAULT NULL COMMENT '出厂日期',
  `remark`          VARCHAR(500) DEFAULT NULL COMMENT '备注',
  `created_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at`      DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='监测设备';

-- 结构物监测配置（关联结构物与监测内容）
CREATE TABLE `structure_monitor_config` (
  `id`              BIGINT NOT NULL AUTO_INCREMENT,
  `structure_id`    BIGINT NOT NULL COMMENT '结构物ID',
  `value_type_id`   BIGINT NOT NULL COMMENT '监测内容ID',
  `created_at`      DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_structure_value_type` (`structure_id`, `value_type_id`),
  KEY `idx_structure_id` (`structure_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='结构物监测配置';

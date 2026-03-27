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

-- 系统用户（不设外键，由应用层保证 department_id 有效）
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

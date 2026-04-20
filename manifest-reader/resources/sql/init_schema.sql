-- =========================================
-- ManifestReader 数据库初始化脚本
-- 数据库: manifestreader
-- 完全去 RuoYi 化，仅含业务核心表
-- =========================================

CREATE DATABASE IF NOT EXISTS `manifestreader` DEFAULT CHARACTER SET utf8mb4;
USE `manifestreader`;

SET FOREIGN_KEY_CHECKS = 0;

-- 1. 提单主表
DROP TABLE IF EXISTS `bl_main`;
CREATE TABLE `bl_main` (
    `id`                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `bl_no`             VARCHAR(50)   NOT NULL COMMENT '提单号',
    `booking_no`        VARCHAR(50)   NULL     COMMENT '订舱号',
    `doc_no`            VARCHAR(50)   NULL     COMMENT '文件编号',
    `serial_no`         VARCHAR(50)   NULL     COMMENT '序列号',
    `vessel_voyage`     VARCHAR(100)  NULL     COMMENT '船名航次',
    `port_of_loading`   VARCHAR(100)  NULL     COMMENT '装运港',
    `port_of_discharge` VARCHAR(100)  NULL     COMMENT '卸货港',
    `place_of_receipt`  VARCHAR(100)  NULL     COMMENT '收货地',
    `place_of_delivery` VARCHAR(100)  NULL     COMMENT '交货地',
    `pre_carriage_by`   VARCHAR(100)  NULL     COMMENT '前段运输',
    `file_path`         VARCHAR(500)  NULL     COMMENT '生成PDF路径',
    `create_by`         VARCHAR(64)   DEFAULT '' COMMENT '创建人(公司编号)',
    `create_time`       DATETIME      NULL,
    `update_by`         VARCHAR(64)   DEFAULT '',
    `update_time`       DATETIME      NULL,
    `remark`            VARCHAR(500)  NULL,
    UNIQUE KEY `uk_bl_no` (`bl_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提单主表';

-- 2. 参与方表
DROP TABLE IF EXISTS `bl_parties`;
CREATE TABLE `bl_parties` (
    `bl_id`          BIGINT UNSIGNED PRIMARY KEY COMMENT '关联 bl_main.id',
    `shipper`        TEXT          NULL COMMENT '发货人',
    `consignee`      TEXT          NULL COMMENT '收货人',
    `notify_party`   TEXT          NULL COMMENT '通知方',
    `carrier_agent`  VARCHAR(255)  NULL COMMENT '承运代理',
    `delivery_agent` TEXT          NULL COMMENT '交付代理',
    FOREIGN KEY (`bl_id`) REFERENCES `bl_main`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提单参与方表';

-- 3. 货物明细表
DROP TABLE IF EXISTS `bl_cargo`;
CREATE TABLE `bl_cargo` (
    `bl_id`               BIGINT UNSIGNED PRIMARY KEY,
    `container_no`        VARCHAR(50)     NULL COMMENT '集装箱号',
    `seal_no`             VARCHAR(50)     NULL COMMENT '封号',
    `container_weight`    DECIMAL(12,2)   NULL COMMENT '箱重(KG)',
    `vgm_weight`          DECIMAL(12,2)   NULL COMMENT 'VGM重量',
    `package_quantity`    INT             NULL COMMENT '件数',
    `package_unit`        VARCHAR(50)     NULL COMMENT '包装单位',
    `goods_description`   TEXT            NULL COMMENT '货物描述',
    `gross_weight_kgs`    DECIMAL(12,2)   NULL COMMENT '总重(KGS)',
    `measurement_cbm`     DECIMAL(12,2)   NULL COMMENT '体积(CBM)',
    `container_seal_info` VARCHAR(255)    NULL COMMENT '封条综合信息',
    `marks`               TEXT            NULL COMMENT '唛头',
    FOREIGN KEY (`bl_id`) REFERENCES `bl_main`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提单货物明细表';

-- 4. 费用与签发表
DROP TABLE IF EXISTS `bl_freight`;
CREATE TABLE `bl_freight` (
    `bl_id`             BIGINT UNSIGNED PRIMARY KEY,
    `service_type`      VARCHAR(50)   NULL COMMENT '服务类型',
    `service_mode`      VARCHAR(50)   NULL COMMENT '服务模式',
    `revenue_tons`      VARCHAR(50)   NULL COMMENT '计费吨',
    `freight_term`      VARCHAR(50)   NULL COMMENT '运费条款',
    `freight_rate`      VARCHAR(50)   NULL COMMENT '费率',
    `prepaid_amount`    VARCHAR(50)   NULL COMMENT '预付金额',
    `collect_amount`    VARCHAR(50)   NULL COMMENT '到付金额',
    `payable_at`        VARCHAR(100)  NULL COMMENT '付款地点',
    `original_bl_count` VARCHAR(20)   NULL COMMENT '正本份数',
    `issue_place`       VARCHAR(100)  NULL COMMENT '签发地',
    `laden_on_board`    TEXT          NULL COMMENT '装船日期',
    FOREIGN KEY (`bl_id`) REFERENCES `bl_main`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提单费用与签发表';

-- 5. PDF 模版配置表
DROP TABLE IF EXISTS `bl_pdf_template`;
CREATE TABLE `bl_pdf_template` (
    `template_id`        BIGINT AUTO_INCREMENT PRIMARY KEY,
    `template_code`      VARCHAR(50)   NOT NULL UNIQUE COMMENT '模版编码',
    `template_name`      VARCHAR(100)  NULL COMMENT '模版名称',
    `template_file_path` VARCHAR(255)  NULL COMMENT '模版文件路径',
    `field_config`       LONGTEXT      NULL COMMENT '字段坐标配置(JSON)',
    `create_by`          VARCHAR(64)   DEFAULT '',
    `create_time`        DATETIME      NULL,
    `update_by`          VARCHAR(64)   DEFAULT '',
    `update_time`        DATETIME      NULL,
    `remark`             VARCHAR(500)  NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PDF模版配置表';

-- 6. 公司用户表
DROP TABLE IF EXISTS `bl_company_user`;
CREATE TABLE `bl_company_user` (
    `user_id`       BIGINT AUTO_INCREMENT PRIMARY KEY,
    `company_name`  VARCHAR(100)  NULL COMMENT '公司名',
    `company_code`  VARCHAR(50)   NOT NULL UNIQUE COMMENT '公司编号(账号)',
    `company_abbr`  VARCHAR(10)   NULL COMMENT '四字缩写(提单前缀)',
    `password`      VARCHAR(100)  NOT NULL COMMENT '密码(BCrypt)',
    `status`        CHAR(1)       DEFAULT '0' COMMENT '0正常 1停用',
    `vip_status`    CHAR(1)       DEFAULT '0' COMMENT '0普通 1会员',
    `expiry_date`   DATETIME      NULL COMMENT '会员到期时间',
    `package_type`  VARCHAR(20)   NULL COMMENT '套餐类型',
    `data_count`    BIGINT        DEFAULT 0 COMMENT '已使用条数',
    `create_by`     VARCHAR(64)   DEFAULT '',
    `create_time`   DATETIME      NULL,
    `update_by`     VARCHAR(64)   DEFAULT '',
    `update_time`   DATETIME      NULL,
    `remark`        VARCHAR(500)  NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='公司用户表';

-- 初始测试账号（密码: test123，BCrypt加密）
INSERT INTO `bl_company_user`(`company_name`, `company_code`, `company_abbr`, `password`, `status`, `create_time`)
VALUES ('测试公司', 'TEST001', 'TEST', '$2a$10$7JB720yubVSZvUI0rEqK/.VqGOZTH.ulu33dHOiBE8ByOhJIbDr.i', '0', NOW());

SET FOREIGN_KEY_CHECKS = 1;

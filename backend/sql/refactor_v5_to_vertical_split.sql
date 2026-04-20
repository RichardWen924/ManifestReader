USE manifestreader;

-- 0. 清理旧表 (如果已存在)
DROP TABLE IF EXISTS `bl_freight`;
DROP TABLE IF EXISTS `bl_cargo`;
DROP TABLE IF EXISTS `bl_parties`;
DROP TABLE IF EXISTS `bl_main`;

-- 1. 创建主表 (Main Tracking Info)
CREATE TABLE IF NOT EXISTS `bl_main` (
    `id`                BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    `bl_no`             VARCHAR(50)                         NOT NULL COMMENT '提单号',
    `booking_no`        VARCHAR(50)                         NULL     COMMENT '订舱号',
    `doc_no`            VARCHAR(50)                         NULL     COMMENT '文件编号',
    `serial_no`         VARCHAR(50)                         NULL     COMMENT '序列号',
    `vessel_voyage`     VARCHAR(100)                        NULL     COMMENT '船名航次',
    `port_of_loading`   VARCHAR(100)                        NULL     COMMENT '装运港',
    `port_of_discharge` VARCHAR(100)                        NULL     COMMENT '卸货港',
    `place_of_receipt`  VARCHAR(100)                        NULL     COMMENT '收货地',
    `place_of_delivery` VARCHAR(100)                        NULL     COMMENT '交货地',
    `pre_carriage_by`   VARCHAR(100)                        NULL     COMMENT '前段运输',
    `file_path`         VARCHAR(500)                        NULL     COMMENT 'PDF文件路径',
    `create_by`         VARCHAR(64)                         DEFAULT '' COMMENT '创建者',
    `created_at`        TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL     COMMENT '创建时间',
    `updated_at`        TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP NULL,
    UNIQUE KEY `uk_bl_no` (`bl_no`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提单主表';

-- 2. 创建参与方表 (Parties)
CREATE TABLE IF NOT EXISTS `bl_parties` (
    `bl_id`             BIGINT UNSIGNED PRIMARY KEY         COMMENT '关联主表ID',
    `shipper`           TEXT                                NULL     COMMENT '发货人',
    `consignee`         TEXT                                NULL     COMMENT '收货人',
    `notify_party`      TEXT                                NULL     COMMENT '通知方',
    `carrier_agent`     VARCHAR(255)                        NULL     COMMENT '承运代理',
    `delivery_agent`    TEXT                                NULL     COMMENT '交付代理',
    FOREIGN KEY (`bl_id`) REFERENCES `bl_main`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提单参与方表';

-- 3. 创建货物与包装表 (Cargo)
CREATE TABLE IF NOT EXISTS `bl_cargo` (
    `bl_id`             BIGINT UNSIGNED PRIMARY KEY         COMMENT '关联主表ID',
    `container_no`      VARCHAR(50)                         NULL     COMMENT '集装箱号',
    `seal_no`           VARCHAR(50)                         NULL     COMMENT '封号',
    `container_weight`  DECIMAL(12, 2)                      NULL     COMMENT '箱重',
    `vgm_weight`        DECIMAL(12, 2)                      NULL     COMMENT 'VGM核实总重',
    `package_quantity`  INT                                 NULL     COMMENT '包装件数',
    `package_unit`      VARCHAR(50)                         NULL     COMMENT '包装单位',
    `goods_description` TEXT                                NULL     COMMENT '货物描述',
    `gross_weight_kgs`  DECIMAL(12, 2)                      NULL     COMMENT '总重 (KGS)',
    `measurement_cbm`   DECIMAL(12, 2)                      NULL     COMMENT '体积 (CBM)',
    `container_seal_info` VARCHAR(255)                      NULL     COMMENT '集装箱封条综合信息',
    `marks`             TEXT                                NULL     COMMENT '唛头',
    FOREIGN KEY (`bl_id`) REFERENCES `bl_main`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提单货物明细表';

-- 4. 创建费用与其它表 (Freight & Other)
CREATE TABLE IF NOT EXISTS `bl_freight` (
    `bl_id`             BIGINT UNSIGNED PRIMARY KEY         COMMENT '关联主表ID',
    `service_type`      VARCHAR(50)                         NULL     COMMENT '服务类型',
    `service_mode`      VARCHAR(50)                         NULL     COMMENT '服务模式',
    `revenue_tons`      VARCHAR(50)                         NULL     COMMENT '计费吨',
    `freight_term`      VARCHAR(50)                         NULL     COMMENT '运费条款',
    `freight_rate`      VARCHAR(50)                         NULL     COMMENT '费率',
    `prepaid_amount`    VARCHAR(50)                         NULL     COMMENT '预付金额',
    `collect_amount`    VARCHAR(50)                         NULL     COMMENT '到付金额',
    `payable_at`        VARCHAR(100)                        NULL     COMMENT '付款地点',
    `original_bl_count` VARCHAR(20)                         NULL     COMMENT '正本份数',
    `issue_place`       VARCHAR(100)                        NULL     COMMENT '签发地',
    `laden_on_board`    TEXT                                NULL     COMMENT '装船日期/说明',
    FOREIGN KEY (`bl_id`) REFERENCES `bl_main`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提单费用与签发信息表';

-- 5. 数据迁移 (Data Migration)
INSERT INTO bl_main (id, bl_no, booking_no, doc_no, serial_no, vessel_voyage, port_of_loading, port_of_discharge, place_of_receipt, place_of_delivery, pre_carriage_by, file_path, created_at)
SELECT id, bl_no, booking_no, doc_no, serial_no, vessel_voyage, port_of_loading, port_of_discharge, place_of_receipt, place_of_delivery, pre_carriage_by, file_path, created_at FROM bill_of_lading_v5;

INSERT INTO bl_parties (bl_id, shipper, consignee, notify_party, carrier_agent, delivery_agent)
SELECT id, shipper, consignee, notify_party, carrier_agent, delivery_agent FROM bill_of_lading_v5;

INSERT INTO bl_cargo (bl_id, container_no, seal_no, container_weight, vgm_weight, package_quantity, package_unit, goods_description, gross_weight_kgs, measurement_cbm, container_seal_info, marks)
SELECT id, container_no, seal_no, container_weight, vgm_weight, package_quantity, package_unit, goods_description, gross_weight_kgs, measurement_cbm, container_seal_info, marks FROM bill_of_lading_v5;

INSERT INTO bl_freight (bl_id, service_type, service_mode, revenue_tons, freight_term, freight_rate, prepaid_amount, collect_amount, payable_at, original_bl_count, issue_place, laden_on_board)
SELECT id, service_type, service_mode, revenue_tons, freight_term, freight_rate, prepaid_amount, collect_amount, payable_at, original_bl_count, issue_place, laden_on_board FROM bill_of_lading_v5;

-- 6. 解耦: 重命名 PDF 模版表
RENAME TABLE sys_pdf_template TO bl_pdf_template;

-- 针对提单所有占位符优化的完整表结构 v5
CREATE TABLE IF NOT EXISTS bill_of_lading_v5
(
    id                  BIGINT UNSIGNED AUTO_INCREMENT PRIMARY KEY,
    
    -- 1. 基础编号与引用
    bl_no               VARCHAR(50)                         NOT NULL COMMENT '提单号',
    booking_no          VARCHAR(50)                         NULL     COMMENT '订舱号',
    doc_no              VARCHAR(50)                         NULL     COMMENT '文件编号',
    serial_no           VARCHAR(50)                         NULL     COMMENT '序列号',
    
    -- 2. 参与方 (TEXT 类型支持换行排版)
    shipper             TEXT                                NULL     COMMENT '发货人',
    consignee           TEXT                                NULL     COMMENT '收货人',
    notify_party        TEXT                                NULL     COMMENT '通知方',
    carrier_agent       VARCHAR(255)                        NULL     COMMENT '承运代理',
    delivery_agent      TEXT                                NULL     COMMENT '交付代理',
    
    -- 3. 运输路由
    pre_carriage_by     VARCHAR(100)                        NULL     COMMENT '前段运输',
    vessel_voyage       VARCHAR(100)                        NULL     COMMENT '船名航次',
    place_of_receipt    VARCHAR(100)                        NULL     COMMENT '收货地',
    port_of_loading     VARCHAR(100)                        NULL     COMMENT '装运港',
    port_of_discharge   VARCHAR(100)                        NULL     COMMENT '卸货港',
    place_of_delivery   VARCHAR(100)                        NULL     COMMENT '交货地',
    
    -- 4. 详细集装箱信息 (新增字段)
    container_no        VARCHAR(50)                         NULL     COMMENT '集装箱号 {{container_number1}}',
    seal_no             VARCHAR(50)                         NULL     COMMENT '封号 {{seal_number1}}',
    container_weight    DECIMAL(12, 2)                      NULL     COMMENT '箱重 {{container_weight1}}',
    vgm_weight          DECIMAL(12, 2)                      NULL     COMMENT 'VGM核实总重 {{VGM}}',
    container_seal_info VARCHAR(255)                        NULL     COMMENT '原始集装箱综合描述',
    
    -- 5. 货物与包装
    package_quantity    INT                                 NULL     COMMENT '包装件数',
    package_unit        VARCHAR(50)                         NULL     COMMENT '包装单位',
    goods_description   TEXT                                NULL     COMMENT '货物描述',
    gross_weight_kgs    DECIMAL(12, 2)                      NULL     COMMENT '总重 (KGS)',
    measurement_cbm     DECIMAL(12, 2)                      NULL     COMMENT '体积 (CBM)',
    
    -- 6. 费用与服务逻辑
    service_type        VARCHAR(50)                         NULL     COMMENT '服务类型',
    service_mode        VARCHAR(50)                         NULL     COMMENT '服务模式 {{service-mode1}}',
    revenue_tons        DECIMAL(12, 3)                      NULL     COMMENT '计费吨',
    freight_term        VARCHAR(50)                         NULL     COMMENT '运费条款',
    freight_rate        VARCHAR(50)                         NULL     COMMENT '费率',
    -- 逻辑：当条款匹配时填充 "AS ARRANGED"
    prepaid_amount      VARCHAR(50)                         NULL     COMMENT '预付金额',
    collect_amount      VARCHAR(50)                         NULL     COMMENT '到付金额',
    payable_at          VARCHAR(100)                        NULL     COMMENT '付款地点',
    
    -- 7. 签发信息
    original_bl_count   VARCHAR(20)                         NULL     COMMENT '正本份数',
    issue_place         VARCHAR(100)                        NULL     COMMENT '签发地',
    laden_on_board      TEXT                                NULL     COMMENT '装船日期/说明',
    
    file_path           VARCHAR(500)                        NULL,
    created_at          TIMESTAMP DEFAULT CURRENT_TIMESTAMP NULL,
    CONSTRAINT uk_bl_no UNIQUE (bl_no)
) COMMENT '提单完整信息表 v5';

-- 如果 bill_of_lading_v4 存在，尝试将数据迁移到 v5（可选）
-- INSERT IGNORE INTO bill_of_lading_v5 (bl_no, booking_no, doc_no, serial_no, shipper, consignee, notify_party, carrier_agent, delivery_agent, pre_carriage_by, vessel_voyage, place_of_receipt, port_of_loading, port_of_discharge, place_of_delivery, container_seal_info, package_quantity, package_unit, goods_description, gross_weight_kgs, measurement_cbm, service_type, service_mode, revenue_tons, freight_term, freight_rate, prepaid_amount, collect_amount, payable_at, original_bl_count, issue_place, laden_on_board, file_path, created_at)
-- SELECT bl_no, booking_no, doc_no, serial_no, shipper, consignee, notify_party, carrier_agent, delivery_agent, pre_carriage_by, vessel_voyage, place_of_receipt, port_of_loading, port_of_discharge, place_of_delivery, container_seal_info, package_quantity, package_unit, goods_description, gross_weight_kgs, measurement_cbm, service_type, service_mode, revenue_tons, freight_term, freight_rate, prepaid_amount, collect_amount, payable_at, original_bl_count, issue_place, laden_on_board, file_path, created_at FROM bill_of_lading_v4;

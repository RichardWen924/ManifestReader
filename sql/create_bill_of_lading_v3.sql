-- 创建bill_of_lading_v3表
CREATE TABLE bill_of_lading_v3 (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    
    -- 1. 基础编号与引用
    bl_no VARCHAR(50) NOT NULL COMMENT 'B/L NO.',
    booking_no VARCHAR(50) COMMENT 'BOOKING NO.',
    doc_no VARCHAR(50) COMMENT 'DOC. NO.',
    serial_no VARCHAR(50) COMMENT 'SERIAL NO.',
    
    -- 2. 运输主体信息
    shipper TEXT COMMENT 'SHIPPER',
    consignee TEXT COMMENT 'CONSIGNEE',
    notify_party TEXT COMMENT 'NOTIFY PARTY',
    carrier_agent VARCHAR(255) COMMENT 'Carrier Agent',
    delivery_agent TEXT COMMENT 'DELIVERY OF GOODS APPLY TO',
    
    -- 3. 运输路径与航次
    vessel_voyage VARCHAR(100) COMMENT 'OCEAN VESSEL/VOY',
    place_of_receipt VARCHAR(100) COMMENT 'PLACE OF RECEIPT',
    port_of_loading VARCHAR(100) COMMENT 'PORT OF LOADING',
    port_of_discharge VARCHAR(100) COMMENT 'PORT OF DISCHARGE',
    place_of_delivery VARCHAR(100) COMMENT 'PLACE OF DELIVERY',
    
    -- 4. 货物与包装明细
    container_seal_info VARCHAR(255) COMMENT '集装箱/封条号',
    package_quantity INT COMMENT 'QUANTITY',
    package_unit VARCHAR(50) COMMENT 'KIND OF PACKAGES',
    goods_description TEXT COMMENT 'DESCRIPTION OF GOODS',
    gross_weight_kgs DECIMAL(12, 2) COMMENT 'GROSS WEIGHT (KGS)',
    measurement_cbm DECIMAL(12, 2) COMMENT 'MEASUREMENT (CBM)',
    
    -- 5. 服务、费用与计费
    service_type VARCHAR(50) COMMENT 'SERVICE TYPE / MODE',
    revenue_tons DECIMAL(12, 3) COMMENT 'REVENUE TONS (计费吨)',
    freight_term VARCHAR(50) COMMENT 'FREIGHT & CHARGES',
    freight_rate VARCHAR(50) COMMENT 'RATE',
    prepaid_amount VARCHAR(50) COMMENT 'PREPAID',
    collect_amount VARCHAR(50) COMMENT 'COLLECT',
    payable_at VARCHAR(100) COMMENT 'PAYABLE AT',
    
    -- 6. 签发与法律效力
    original_bl_count VARCHAR(20) COMMENT 'NUMBER OF ORIGINAL B/L(S)',
    issue_place VARCHAR(100) COMMENT 'PLACE AND DATE OF ISSUE',
    laden_on_board TEXT COMMENT 'LADEN ON BOARD',
    
    -- 7. 文件路径
    file_path VARCHAR(500) COMMENT 'PDF文件路径',
    
    -- 8. 时间戳
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='提单信息表v3';

-- 创建索引
CREATE INDEX idx_bl_no ON bill_of_lading_v3(bl_no);
CREATE INDEX idx_booking_no ON bill_of_lading_v3(booking_no);
CREATE INDEX idx_created_at ON bill_of_lading_v3(created_at);

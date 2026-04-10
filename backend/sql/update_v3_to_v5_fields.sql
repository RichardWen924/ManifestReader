-- 为 bill_of_lading_v3 补充 V5/V4 缺失的字段
ALTER TABLE bill_of_lading_v3
ADD COLUMN IF NOT EXISTS pre_carriage_by     VARCHAR(100) NULL COMMENT '前段运输' AFTER delivery_agent,
ADD COLUMN IF NOT EXISTS container_no        VARCHAR(50)  NULL COMMENT '集装箱号' AFTER place_of_delivery,
ADD COLUMN IF NOT EXISTS seal_no             VARCHAR(50)  NULL COMMENT '封号' AFTER container_no,
ADD COLUMN IF NOT EXISTS container_weight    DECIMAL(12, 2) NULL COMMENT '箱重' AFTER seal_no,
ADD COLUMN IF NOT EXISTS vgm_weight          DECIMAL(12, 2) NULL COMMENT 'VGM核实总重' AFTER container_weight,
ADD COLUMN IF NOT EXISTS service_mode        VARCHAR(50)  NULL COMMENT '服务模式' AFTER service_type;

-- 确保旧数据表结构与 v5 逻辑完全同步（补齐 v5SQL 中提到的所有新特质）
-- 注意：v3 可能原本就有部分字段，ADD COLUMN IF NOT EXISTS 是安全的。

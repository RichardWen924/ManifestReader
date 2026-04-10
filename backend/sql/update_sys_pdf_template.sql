-- 修改现有的 sys_pdf_template 表结构
-- 此脚本会安全地添加缺失的列，不会删除现有数据

-- 1. 检查并添加 template_file_path 列（如果不存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'sys_pdf_template' 
  AND COLUMN_NAME = 'template_file_path';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_pdf_template ADD COLUMN template_file_path varchar(500) NOT NULL COMMENT ''PDF模版文件路径'' AFTER template_name',
    'SELECT ''Column template_file_path already exists'' AS Info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2. 检查并添加 field_config 列（如果不存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'sys_pdf_template' 
  AND COLUMN_NAME = 'field_config';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_pdf_template ADD COLUMN field_config text COMMENT ''字段配置(JSON格式)'' AFTER template_file_path',
    'SELECT ''Column field_config already exists'' AS Info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 2.1 检查并添加 create_by 列（如果不存在）
SET @col_exists = 0;
SELECT COUNT(*) INTO @col_exists 
FROM information_schema.COLUMNS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'sys_pdf_template' 
  AND COLUMN_NAME = 'create_by';

SET @sql = IF(@col_exists = 0,
    'ALTER TABLE sys_pdf_template ADD COLUMN create_by varchar(64) DEFAULT '''' COMMENT ''创建者'' AFTER create_time',
    'SELECT ''Column create_by already exists'' AS Info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 3. 确保 template_code 列有唯一索引
SET @index_exists = 0;
SELECT COUNT(*) INTO @index_exists 
FROM information_schema.STATISTICS 
WHERE TABLE_SCHEMA = DATABASE() 
  AND TABLE_NAME = 'sys_pdf_template' 
  AND INDEX_NAME = 'uk_template_code';

SET @sql = IF(@index_exists = 0,
    'ALTER TABLE sys_pdf_template ADD UNIQUE KEY uk_template_code (template_code)',
    'SELECT ''Index uk_template_code already exists'' AS Info');
PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- 4. 插入或更新示例数据
INSERT INTO sys_pdf_template (template_code, template_name, template_file_path, field_config, create_time, remark)
VALUES (
    'booking_standard',
    '提单标准模版',
    'classpath:模版.pdf',
    '{
  "booking_no": {"page": 1, "x": 420, "y": 710, "w": 120, "h": 15},
  "shipper": {"page": 1, "x": 50, "y": 650, "w": 250, "h": 60},
  "consignee": {"page": 1, "x": 50, "y": 580, "w": 250, "h": 60},
  "notify_party": {"page": 1, "x": 50, "y": 510, "w": 250, "h": 60},
  "vessel_voyage": {"page": 1, "x": 320, "y": 650, "w": 220, "h": 15},
  "port_of_loading": {"page": 1, "x": 320, "y": 620, "w": 220, "h": 15},
  "port_of_discharge": {"page": 1, "x": 320, "y": 590, "w": 220, "h": 15},
  "place_of_delivery": {"page": 1, "x": 320, "y": 560, "w": 220, "h": 15},
  "cargo_description": {"page": 1, "x": 50, "y": 400, "w": 490, "h": 80},
  "cargo_quantity": {"page": 1, "x": 50, "y": 370, "w": 100, "h": 15},
  "cargo_gross_weight": {"page": 1, "x": 160, "y": 370, "w": 100, "h": 15},
  "cargo_measurement": {"page": 1, "x": 270, "y": 370, "w": 100, "h": 15},
  "container_no": {"page": 1, "x": 50, "y": 340, "w": 150, "h": 15},
  "seal_no": {"page": 1, "x": 210, "y": 340, "w": 100, "h": 15}
}',
    NOW(),
    '提单标准模版，包含基础字段坐标'
)
ON DUPLICATE KEY UPDATE
    template_name = VALUES(template_name),
    template_file_path = VALUES(template_file_path),
    field_config = VALUES(field_config),
    update_time = NOW(),
    remark = VALUES(remark);

-- 5. 验证结果
SELECT 
    template_id,
    template_code,
    template_name,
    template_file_path,
    CASE 
        WHEN field_config IS NOT NULL THEN CONCAT(LEFT(field_config, 50), '...')
        ELSE NULL 
    END AS field_config_preview,
    create_time,
    update_time
FROM sys_pdf_template
WHERE template_code = 'booking_standard';

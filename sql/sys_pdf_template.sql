-- 创建 PDF 模版表
CREATE TABLE IF NOT EXISTS `sys_pdf_template` (
  `template_id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模版ID',
  `template_code` varchar(100) NOT NULL COMMENT '模版编码',
  `template_name` varchar(200) DEFAULT NULL COMMENT '模版名称',
  `template_file_path` varchar(500) NOT NULL COMMENT 'PDF模版文件路径',
  `field_config` text COMMENT '字段配置(JSON格式)',
  `create_time` datetime DEFAULT NULL COMMENT '创建时间',
  `update_time` datetime DEFAULT NULL COMMENT '更新时间',
  `remark` varchar(500) DEFAULT NULL COMMENT '备注',
  PRIMARY KEY (`template_id`),
  UNIQUE KEY `uk_template_code` (`template_code`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='PDF模版配置表';

-- 插入示例数据
INSERT INTO `sys_pdf_template` (`template_code`, `template_name`, `template_file_path`, `field_config`, `create_time`, `remark`) VALUES
('booking_standard', '提单标准模版', 'classpath:模版.pdf', 
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
NOW(), '提单标准模版，包含基础字段坐标');

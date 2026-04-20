package com.manifest.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.manifest.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * PDF 模版配置表 bl_pdf_template
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bl_pdf_template")
public class PdfTemplate extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long templateId;

    /** 模版编码 */
    private String templateCode;

    /** 模版名称 */
    private String templateName;

    /** 模版文件路径 */
    private String templateFilePath;

    /** 字段坐标配置(JSON) */
    private String fieldConfig;
}

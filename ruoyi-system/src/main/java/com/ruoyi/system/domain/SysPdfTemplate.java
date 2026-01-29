package com.ruoyi.system.domain;

import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * PDF模版配置对象 sys_pdf_template
 * 
 * @author ruoyi
 * @date 2026-01-28
 */
public class SysPdfTemplate extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 模版ID */
    private Long templateId;

    /** 模版编码 */
    @Excel(name = "模版编码")
    private String templateCode;

    /** 模版名称 */
    @Excel(name = "模版名称")
    private String templateName;

    /** PDF模版文件路径 */
    @Excel(name = "模版文件路径")
    private String templateFilePath;

    /** 字段配置(JSON格式) */
    private String fieldConfig;

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateFilePath() {
        return templateFilePath;
    }

    public void setTemplateFilePath(String templateFilePath) {
        this.templateFilePath = templateFilePath;
    }

    public String getFieldConfig() {
        return fieldConfig;
    }

    public void setFieldConfig(String fieldConfig) {
        this.fieldConfig = fieldConfig;
    }

    @Override
    public String toString() {
        return "SysPdfTemplate{" +
                "templateId=" + templateId +
                ", templateCode='" + templateCode + '\'' +
                ", templateName='" + templateName + '\'' +
                ", templateFilePath='" + templateFilePath + '\'' +
                ", fieldConfig='" + fieldConfig + '\'' +
                '}';
    }
}

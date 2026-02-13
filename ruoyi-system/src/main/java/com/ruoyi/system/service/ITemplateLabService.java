package com.ruoyi.system.service;

import java.util.List;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.system.domain.SysTemplateMapping;

/**
 * 智能模版实验室Service接口
 * 
 * @author Richard
 */
public interface ITemplateLabService {
    /**
     * 分析文档获取映射建议
     */
    public List<SysTemplateMapping> analyzeDocument(MultipartFile file);

    /**
     * 实时预览：将文档中的原文替换为 {{变量}}
     */
    public byte[] previewTemplate(MultipartFile file, List<SysTemplateMapping> mappings);

    /**
     * 保存模版
     */
    public String saveTemplate(MultipartFile file, List<SysTemplateMapping> mappings, String templateName);

    /**
     * 使用模版导出docx：将业务数据填充到模版的占位符中
     */
    /**
     * 使用模版导出docx：将业务数据填充到模版的占位符中
     */
    public byte[] exportWithTemplate(Long templateId, java.util.Map<String, Object> businessData);

    /**
     * 获取模版文件的HTML内容（用于在线编辑）
     */
    public String getTemplateHtml(MultipartFile file);

    /**
     * 将HTML内容转换为Docx文件
     */
    public java.io.File convertHtmlToDocx(String htmlContent);
}

package com.ruoyi.system.service.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.apache.poi.xwpf.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.ruoyi.system.domain.SysTemplateMapping;
import com.ruoyi.system.service.ITemplateLabService;
import com.ruoyi.common.utils.StringUtils;

@Service
public class TemplateLabServiceImpl implements ITemplateLabService {
    private static final Logger log = LoggerFactory.getLogger(TemplateLabServiceImpl.class);

    @Override
    public List<SysTemplateMapping> analyzeDocument(MultipartFile file) {
        // TODO: 集成 Dify API 进行智能分析
        // 目前返回 Mock 数据用于前端联调
        List<SysTemplateMapping> list = new ArrayList<>();
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
            // 简单的文本提取展示
            for (XWPFParagraph p : doc.getParagraphs()) {
                String text = p.getText();
                if (StringUtils.isNotEmpty(text) && text.length() < 100 && text.contains(":")) {
                    SysTemplateMapping m = new SysTemplateMapping();
                    m.setOriginalText(text.split(":")[0].trim());
                    m.setPlaceholderKey(m.getOriginalText().toLowerCase().replaceAll("\\s+", "_"));
                    m.setDescription("Detected field");
                    list.add(m);
                }
            }
        } catch (IOException e) {
            log.error("分析文档失败", e);
        }
        return list;
    }

    @Override
    public byte[] previewTemplate(MultipartFile file, List<SysTemplateMapping> mappings) {
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            replaceText(doc, mappings);
            doc.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            log.error("预览模版失败", e);
            return new byte[0];
        }
    }

    @Override
    public String saveTemplate(MultipartFile file, List<SysTemplateMapping> mappings, String templateName) {
        // 实际上也是执行替换并保存到文件系统
        return "Not implemented yet";
    }

    private void replaceText(XWPFDocument doc, List<SysTemplateMapping> mappings) {
        for (SysTemplateMapping m : mappings) {
            String target = m.getOriginalText();
            String replacement = "{{" + m.getPlaceholderKey() + "}}";

            // 遍历段落
            for (XWPFParagraph p : doc.getParagraphs()) {
                for (XWPFRun r : p.getRuns()) {
                    String text = r.getText(0);
                    if (text != null && text.contains(target)) {
                        r.setText(text.replace(target, replacement), 0);
                    }
                }
            }

            // 遍历表格
            for (XWPFTable tbl : doc.getTables()) {
                for (XWPFTableRow row : tbl.getRows()) {
                    for (XWPFTableCell cell : row.getTableCells()) {
                        for (XWPFParagraph p : cell.getParagraphs()) {
                            for (XWPFRun r : p.getRuns()) {
                                String text = r.getText(0);
                                if (text != null && text.contains(target)) {
                                    r.setText(text.replace(target, replacement), 0);
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

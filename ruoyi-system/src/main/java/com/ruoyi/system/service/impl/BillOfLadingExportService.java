package com.ruoyi.system.service.impl;

import com.deepoove.poi.XWPFTemplate;
import com.ruoyi.system.utils.BillOfLadingDataProcessor;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.jodconverter.local.LocalConverter;
import org.jodconverter.local.office.LocalOfficeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.util.Map;

/**
 * 提单导出服务实现类
 * 使用 poi-tl 填充 Word 模板，并转换为 PDF
 */
@Service
public class BillOfLadingExportService {
    private static final Logger log = LoggerFactory.getLogger(BillOfLadingExportService.class);

    // 默认模板路径（用户指定的路径）
    private static final String DEFAULT_TEMPLATE_PATH = "/Users/richard/Downloads/1.docx";

    /**
     * 导出提单为 PDF 字节数组
     * 
     * @param difyData Dify 返回的原始数据
     * @return PDF 字节数组
     */
    public byte[] exportToPdf(Map<String, Object> difyData) {
        try {
            // 1. 数据预处理
            Map<String, Object> processedData = BillOfLadingDataProcessor.process(difyData);
            log.info("提单数据预处理完成");

            // 2. 填充 Word 模板
            byte[] wordBytes = fillWordTemplate(processedData, DEFAULT_TEMPLATE_PATH);
            log.info("Word 模板填充完成，大小: {} 字节", wordBytes.length);

            // 3. 转换为 PDF
            byte[] pdfBytes = convertWordToPdf(wordBytes);
            log.info("PDF 转换完成，大小: {} 字节", pdfBytes.length);

            return pdfBytes;
        } catch (Exception e) {
            log.error("提单导出 PDF 失败", e);
            throw new RuntimeException("提单导出 PDF 失败: " + e.getMessage(), e);
        }
    }

    /**
     * 使用 poi-tl 填充 Word 模板
     */
    private byte[] fillWordTemplate(Map<String, Object> data, String templatePath) throws Exception {
        File templateFile = new File(templatePath);
        if (!templateFile.exists()) {
            throw new RuntimeException("模板文件不存在: " + templatePath);
        }

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFTemplate template = XWPFTemplate.compile(templatePath).render(data);
            template.write(out);
            template.close();
            return out.toByteArray();
        }
    }

    /**
     * 将 Word 字节数组转换为 PDF 字节数组
     * 使用 JODConverter + 本地 LibreOffice
     */
    private byte[] convertWordToPdf(byte[] wordBytes) throws Exception {
        // 在 macOS 上，LibreOffice 通常安装在 /Applications/LibreOffice.app
        // JODConverter 会尝试自动检测，但我们可以手动配置

        LocalOfficeManager officeManager = LocalOfficeManager.builder()
                .maxTasksPerProcess(10)
                .build();

        try {
            officeManager.start();
            log.info("LibreOffice 本地管理器已启动");

            DocumentConverter converter = LocalConverter.make(officeManager);
            try (ByteArrayInputStream in = new ByteArrayInputStream(wordBytes);
                    ByteArrayOutputStream out = new ByteArrayOutputStream()) {

                converter.convert(in).to(out).as(DefaultDocumentFormatRegistry.PDF).execute();
                return out.toByteArray();
            }
        } finally {
            if (officeManager.isRunning()) {
                officeManager.stop();
                log.info("LibreOffice 本地管理器已停止");
            }
        }
    }
}

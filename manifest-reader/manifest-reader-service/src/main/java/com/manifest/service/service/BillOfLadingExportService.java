package com.manifest.service.service;

import com.deepoove.poi.XWPFTemplate;
import com.manifest.common.exception.ServiceException;
import com.manifest.model.entity.BillOfLading;
import com.manifest.service.utils.BillOfLadingDataProcessor;
import com.manifest.service.utils.PdfEditUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jodconverter.core.DocumentConverter;
import org.jodconverter.core.document.DefaultDocumentFormatRegistry;
import org.springframework.stereotype.Service;

import java.io.*;
import java.util.Map;

/**
 * 提单高保真导出服务
 * 流程：Word模版填充(poi-tl) -> 转PDF(JODConverter) -> 坐标级修正(iText7)
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BillOfLadingExportService {

    private final DocumentConverter converter;
    private final PdfEditUtils pdfEditUtils;

    /**
     * 导出提单为 PDF
     * @param bl 提单数据实体
     * @param templatePath Word模版路径
     * @param config 坐标配置
     * @return 最终生成的 PDF 字节流
     */
    public byte[] exportPdf(BillOfLading bl, String templatePath, Map config) {
        try {
            // 1. 数据转换 (实体 -> 模版Map)
            Map<String, Object> dataMap = BillOfLadingDataProcessor.process(objectToMap(bl));

            // 2. Word 渲染
            ByteArrayOutputStream wordOut = new ByteArrayOutputStream();
            XWPFTemplate template = XWPFTemplate.compile(templatePath).render(dataMap);
            template.write(wordOut);
            template.close();

            // 3. Word 转 PDF (初步)
            ByteArrayInputStream wordIn = new ByteArrayInputStream(wordOut.toByteArray());
            ByteArrayOutputStream pdfOut = new ByteArrayOutputStream();
            converter.convert(wordIn).to(pdfOut).as(DefaultDocumentFormatRegistry.PDF).execute();

            // 4. iText7 坐标级修正 (如果提供了配置)
            if (config != null && !config.isEmpty()) {
                ByteArrayOutputStream finalPdf = new ByteArrayOutputStream();
                pdfEditUtils.editPdf(new ByteArrayInputStream(pdfOut.toByteArray()), finalPdf, dataMap, config);
                return finalPdf.toByteArray();
            }

            return pdfOut.toByteArray();
        } catch (Exception e) {
            log.error("[Export] 提单导出失败", e);
            throw new ServiceException("生成PDF失败: " + e.getMessage());
        }
    }

    private Map<String, Object> objectToMap(Object obj) {
        // 简单实现，实际可用 BeanUtil
        return cn.hutool.core.bean.BeanUtil.beanToMap(obj);
    }
}

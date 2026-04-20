package com.manifest.service.utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.manifest.model.dto.BillOfLadingDto.FieldLocation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

/**
 * PDF 像素级编辑工具 (iText7)
 * 核心逻辑：抹除指定区域内容，并在原位重写新内容
 */
@Slf4j
@Component
public class PdfEditUtils {

    /**
     * 根据坐标配置填充 PDF
     */
    public void editPdf(InputStream source, OutputStream target, 
                        Map<String, Object> data, Map<String, FieldLocation> config) {
        try (PdfDocument pdfDoc = new PdfDocument(new PdfReader(source), new PdfWriter(target))) {
            PdfFont font = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", false);

            for (Map.Entry<String, FieldLocation> entry : config.entrySet()) {
                String key = entry.getKey();
                FieldLocation loc = entry.getValue();
                Object value = data.get(key);
                
                if (value == null || loc == null) continue;

                PdfCanvas canvas = new PdfCanvas(pdfDoc.getPage(loc.getPage()));
                
                // 1. 抹除背景 (白色填充)
                canvas.saveState()
                      .setFillColor(ColorConstants.WHITE)
                      .rectangle(loc.getX(), loc.getY(), loc.getW(), loc.getH())
                      .fill()
                      .restoreState();

                // 2. 写入文字
                canvas.beginText()
                      .setFontAndSize(font, 9)
                      .moveText(loc.getX() + 2, loc.getY() + 2)
                      .showText(value.toString())
                      .endText();
            }
        } catch (Exception e) {
            log.error("[PDF] 编辑失败", e);
        }
    }
}

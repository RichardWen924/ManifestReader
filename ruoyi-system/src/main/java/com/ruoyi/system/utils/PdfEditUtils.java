package com.ruoyi.system.utils;

import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.Rectangle;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfReader;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.properties.TextAlignment;
import com.ruoyi.system.domain.BookingConsolidatedDto;
import com.ruoyi.system.domain.BookingConsolidatedDto.FieldLocation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * PDF 编辑工具类 (基于 iText7)
 * 实现：抹除原数据并在同一位置重写新数据
 */
public class PdfEditUtils {
    private static final Logger log = LoggerFactory.getLogger(PdfEditUtils.class);

    /**
     * 修改 PDF
     * @param srcPath 原始文件路径
     * @param dto 包含新数据和坐标信息的 DTO
     * @return 修改后的文件路径
     */
    public static String modifyPdf(String srcPath, BookingConsolidatedDto dto) throws IOException {
        String destPath = srcPath.substring(0, srcPath.lastIndexOf(".")) + "_modified.pdf";
        
        try (PdfReader reader = new PdfReader(srcPath);
             PdfWriter writer = new PdfWriter(destPath);
             PdfDocument pdfDoc = new PdfDocument(reader, writer)) {

            // 加载中文字体 (STSong-Light)
            PdfFont chineseFont = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H", PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);

            Map<String, FieldLocation> locations = dto.getFieldLocations();
            Map<String, Object> data = (Map<String, Object>) dto.getBusinessData();

            for (Map.Entry<String, FieldLocation> entry : locations.entrySet()) {
                String fieldName = entry.getKey();
                FieldLocation loc = entry.getValue();
                Object value = data.get(fieldName);

                if (value == null || loc == null) continue;

                // 获取对应页面
                int pageNum = loc.getPage() > 0 ? loc.getPage() : 1;
                if (pageNum > pdfDoc.getNumberOfPages()) continue;
                
                PdfPage page = pdfDoc.getPage(pageNum);
                PdfCanvas pdfCanvas = new PdfCanvas(page);

                // 1. 抹除操作：绘制白色矩形覆盖旧数据
                // 坐标系：左下角为原点 (x, y, w, h)
                Rectangle rect = new Rectangle(loc.getX(), loc.getY(), loc.getW(), loc.getH());
                pdfCanvas.saveState()
                        .setFillColor(ColorConstants.WHITE)
                        .rectangle(rect)
                        .fill()
                        .restoreState();

                // 2. 重写操作：在同一位置写入新数据
                try (Canvas canvas = new Canvas(page, rect)) {
                    canvas.add(new Paragraph(String.valueOf(value))
                            .setFont(chineseFont)
                            .setFontSize(8) // 可根据高度动态调整
                            .setMargin(0)
                            .setPadding(0)
                            .setTextAlignment(TextAlignment.LEFT));
                }
            }
        } catch (Exception e) {
            log.error("PDF 修改失败: {}", e.getMessage(), e);
            throw new IOException("PDF 修改失败: " + e.getMessage());
        }

        return destPath;
    }
}

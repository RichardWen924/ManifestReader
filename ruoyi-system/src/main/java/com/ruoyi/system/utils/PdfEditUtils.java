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
     * 
     * @param srcPath 原始文件路径 (支持 classpath: 前缀或绝对路径)
     * @param dto     包含新数据和坐标信息的 DTO
     * @return 修改后的文件路径
     */
    public static String modifyPdf(String srcPath, BookingConsolidatedDto dto) throws IOException {
        log.info("开始修改 PDF，模版路径: {}", srcPath);

        // 生成输出文件路径
        String destPath = generateOutputPath(srcPath);

        try (PdfReader reader = createPdfReader(srcPath);
                PdfWriter writer = new PdfWriter(destPath);
                PdfDocument pdfDoc = new PdfDocument(reader, writer)) {

            log.info("PDF 文档加载成功，页数: {}", pdfDoc.getNumberOfPages());

            // 加载中文字体 (STSong-Light)
            PdfFont chineseFont = PdfFontFactory.createFont("STSong-Light", "UniGB-UCS2-H",
                    PdfFontFactory.EmbeddingStrategy.PREFER_NOT_EMBEDDED);

            Map<String, FieldLocation> locations = dto.getFieldLocations();
            Map<String, Object> data = (Map<String, Object>) dto.getBusinessData();

            log.info("准备处理字段，坐标配置数量: {}, 业务数据数量: {}",
                    locations != null ? locations.size() : 0,
                    data != null ? data.size() : 0);

            if (locations == null || locations.isEmpty()) {
                log.warn("字段坐标配置为空，PDF将不会被修改");
                return destPath;
            }

            if (data == null || data.isEmpty()) {
                log.warn("业务数据为空，PDF将不会被修改");
                return destPath;
            }

            // 应用业务规则（blNo/bookingNo同步、运费逻辑等）
            log.info("应用业务规则...");
            BillOfLadingValidator.applyBusinessRules(data);
            log.info("业务规则应用完成");

            int processedCount = 0;

            for (Map.Entry<String, FieldLocation> entry : locations.entrySet()) {
                String fieldName = entry.getKey();
                FieldLocation loc = entry.getValue();
                Object value = data.get(fieldName);

                // 跳过null值
                if (value == null) {
                    log.debug("字段 {} 的值为null，跳过", fieldName);
                    continue;
                }

                // 获取对应页面
                int pageNum = loc.getPage() > 0 ? loc.getPage() : 1;
                if (pageNum > pdfDoc.getNumberOfPages()) {
                    log.warn("字段 {} 的页码 {} 超出范围，跳过", fieldName, pageNum);
                    continue;
                }

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
                String textValue = String.valueOf(value);
                try (Canvas canvas = new Canvas(page, rect)) {
                    canvas.add(new Paragraph(textValue)
                            .setFont(chineseFont)
                            .setFontSize(8) // 可根据高度动态调整
                            .setMargin(0)
                            .setPadding(0)
                            .setTextAlignment(TextAlignment.LEFT));
                }

                processedCount++;
                log.debug("已处理字段: {} = {}, 位置: page={}, x={}, y={}, w={}, h={}",
                        fieldName, textValue, pageNum, loc.getX(), loc.getY(), loc.getW(), loc.getH());
            }

            log.info("PDF修改完成，共处理 {} 个字段", processedCount);

            // 表单扁平化：将PDF表单字段转为不可编辑状态
            flattenPdfForms(pdfDoc);
        } catch (Exception e) {
            log.error("PDF 修改失败: {}", e.getMessage(), e);
            throw new IOException("PDF 修改失败: " + e.getMessage());
        }

        return destPath;
    }

    /**
     * 扁平化PDF表单
     * 将可编辑的表单字段转换为静态内容
     * 
     * @param pdfDoc PDF文档
     */
    private static void flattenPdfForms(PdfDocument pdfDoc) {
        try {
            if (pdfDoc.getCatalog().getPdfObject().getAsDictionary(com.itextpdf.kernel.pdf.PdfName.AcroForm) != null) {
                com.itextpdf.forms.PdfAcroForm form = com.itextpdf.forms.PdfAcroForm.getAcroForm(pdfDoc, false);
                if (form != null) {
                    form.flattenFields();
                    log.info("PDF表单已扁平化，现在为不可编辑状态");
                } else {
                    log.debug("PDF不包含表单字段，跳过扁平化");
                }
            }
        } catch (Exception e) {
            log.warn("表单扁平化失败（不影响主流程）: {}", e.getMessage());
        }
    }

    /**
     * 创建 PdfReader，支持 classpath 资源和文件系统路径
     * 
     * @param path 路径 (支持 "classpath:模版.pdf" 或 "/absolute/path/to/file.pdf")
     */
    private static PdfReader createPdfReader(String path) throws IOException {
        if (path.startsWith("classpath:")) {
            // 从 classpath 加载
            String resourcePath = path.substring("classpath:".length());
            log.info("从 classpath 加载 PDF 资源: {}", resourcePath);

            java.io.InputStream inputStream = PdfEditUtils.class.getClassLoader().getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IOException("无法找到 classpath 资源: " + resourcePath);
            }
            return new PdfReader(inputStream);
        } else {
            // 从文件系统加载
            log.info("从文件系统加载 PDF: {}", path);
            File file = new File(path);
            if (!file.exists()) {
                throw new IOException("文件不存在: " + path);
            }
            return new PdfReader(path);
        }
    }

    /**
     * 生成输出文件路径
     * 
     * @param srcPath 源文件路径
     */
    private static String generateOutputPath(String srcPath) {
        // 统一输出到用户主目录下的pdf_output文件夹
        String userHome = System.getProperty("user.home");
        String outputDir = userHome + File.separator + "pdf_output";

        // 创建输出目录
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
            log.info("创建PDF输出目录: {}", outputDir);
        }

        // 生成文件名：原文件名_时间戳_modified.pdf
        String fileName;
        if (srcPath.startsWith("classpath:")) {
            fileName = srcPath.substring(srcPath.lastIndexOf("/") + 1);
        } else {
            fileName = new File(srcPath).getName();
        }

        String baseName = fileName.substring(0, fileName.lastIndexOf("."));
        String timestamp = String.valueOf(System.currentTimeMillis());
        String outputPath = outputDir + File.separator + baseName + "_" + timestamp + "_modified.pdf";

        log.info("PDF将保存到: {}", outputPath);
        return outputPath;
    }
}

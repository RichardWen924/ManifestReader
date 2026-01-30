package com.ruoyi.system.utils;

import com.openhtmltopdf.pdfboxout.PdfRendererBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * HTML转PDF工具类
 * 使用OpenHTML to PDF库将HTML字符串转换为PDF文档
 * 
 * @author ruoyi
 * @date 2026-01-30
 */
public class HtmlToPdfConverter {

    private static final Logger log = LoggerFactory.getLogger(HtmlToPdfConverter.class);

    /**
     * 将HTML字符串转换为PDF字节数组
     * 
     * @param html HTML内容（包含CSS样式）
     * @return PDF字节数组
     * @throws IOException 转换失败时抛出异常
     */
    public static byte[] convertHtmlToPdfBytes(String html) throws IOException {
        if (html == null || html.trim().isEmpty()) {
            throw new IllegalArgumentException("HTML内容不能为空");
        }

        log.info("开始转换HTML为PDF，HTML长度: {} 字符", html.length());

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            // 创建PDF渲染器
            PdfRendererBuilder builder = new PdfRendererBuilder();

            // 包装HTML为完整的XHTML文档
            String xhtml = wrapAsXhtml(html);

            // 配置渲染器
            builder.useFastMode();
            builder.withHtmlContent(xhtml, null);
            builder.toStream(outputStream);

            // 执行渲染
            builder.run();

            byte[] pdfBytes = outputStream.toByteArray();
            log.info("HTML转PDF成功，PDF大小: {} 字节", pdfBytes.length);

            return pdfBytes;

        } catch (Exception e) {
            log.error("HTML转PDF失败", e);
            throw new IOException("HTML转PDF转换失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将HTML片段包装为完整的XHTML文档
     * OpenHTML to PDF需要well-formed的XHTML
     * 
     * @param htmlFragment HTML片段
     * @return 完整的XHTML文档
     */
    private static String wrapAsXhtml(String htmlFragment) {
        // 检查是否已经是完整的HTML文档
        if (htmlFragment.trim().toLowerCase().startsWith("<!doctype") ||
                htmlFragment.trim().toLowerCase().startsWith("<html")) {
            // 清理HTML使其符合XHTML标准
            return cleanupHtmlToXhtml(htmlFragment);
        }

        // 清理HTML片段
        String cleanedFragment = cleanupHtmlToXhtml(htmlFragment);

        // 包装为XHTML文档
        StringBuilder xhtml = new StringBuilder();
        xhtml.append("<!DOCTYPE html>");
        xhtml.append("<html>");
        xhtml.append("<head>");
        xhtml.append("<meta charset=\"UTF-8\"/>");
        xhtml.append("<style>");
        xhtml.append("body { margin: 0; padding: 0; }");
        xhtml.append("</style>");
        xhtml.append("</head>");
        xhtml.append("<body>");
        xhtml.append(cleanedFragment);
        xhtml.append("</body>");
        xhtml.append("</html>");

        return xhtml.toString();
    }

    /**
     * 清理HTML使其符合XHTML标准
     * 修复常见的未闭合标签问题
     * 
     * @param html 原始HTML
     * @return 符合XHTML标准的HTML
     */
    private static String cleanupHtmlToXhtml(String html) {
        String cleaned = html;

        // 修复自闭合标签（必须使用 /> 结尾）
        cleaned = cleaned.replaceAll("<br\\s*>", "<br/>");
        cleaned = cleaned.replaceAll("<hr\\s*>", "<hr/>");
        cleaned = cleaned.replaceAll("<img([^>]*)>", "<img$1/>");
        cleaned = cleaned.replaceAll("<input([^>]*)>", "<input$1/>");
        cleaned = cleaned.replaceAll("<meta([^>]*)>", "<meta$1/>");
        cleaned = cleaned.replaceAll("<link([^>]*)>", "<link$1/>");

        // 确保已有的自闭合标签格式正确（避免双斜杠）
        cleaned = cleaned.replaceAll("//+>", "/>");

        return cleaned;
    }

    /**
     * 将HTML字符串转换为PDF字节数组（带自定义CSS）
     * 
     * @param html HTML内容
     * @param css  自定义CSS样式
     * @return PDF字节数组
     * @throws IOException 转换失败时抛出异常
     */
    public static byte[] convertHtmlToPdfBytesWithCss(String html, String css) throws IOException {
        if (html == null || html.trim().isEmpty()) {
            throw new IllegalArgumentException("HTML内容不能为空");
        }

        // 将CSS注入到HTML中
        String htmlWithCss = injectCss(html, css);
        return convertHtmlToPdfBytes(htmlWithCss);
    }

    /**
     * 将CSS样式注入到HTML中
     * 
     * @param html 原始HTML
     * @param css  CSS样式
     * @return 注入CSS后的HTML
     */
    private static String injectCss(String html, String css) {
        if (css == null || css.trim().isEmpty()) {
            return html;
        }

        // 如果HTML包含<head>标签，在其中插入<style>
        if (html.contains("<head>")) {
            return html.replaceFirst("<head>",
                    "<head><style>" + css + "</style>");
        } else {
            // 否则在包装时添加
            return html;
        }
    }
}

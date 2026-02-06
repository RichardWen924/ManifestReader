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
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.file.FileUploadUtils;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

@Service
public class TemplateLabServiceImpl implements ITemplateLabService {
    private static final Logger log = LoggerFactory.getLogger(TemplateLabServiceImpl.class);

    private static final String DIFY_API_KEY_ANALYZE = "app-J8tEYmWBzqHsDqXlvDfNHn0r";
    private static final String DIFY_BASE_URL = "http://localhost/v1";

    @Override
    public List<SysTemplateMapping> analyzeDocument(MultipartFile file) {
        log.info("开始智能分析模版文档: {}", file.getOriginalFilename());
        List<SysTemplateMapping> list = new ArrayList<>();

        // 1. 保存临时文件供 Dify 读取
        String tempPath = "";
        try {
            tempPath = FileUploadUtils.upload(RuoYiConfig.getProfile(), file);
            String fullPath = RuoYiConfig.getProfile() + tempPath.replaceFirst(Constants.RESOURCE_PREFIX, "");

            // 2. 调用 Dify 工作流
            JSONObject result = callDifyWorkflow(fullPath, DIFY_API_KEY_ANALYZE);

            if (result != null && result.containsKey("mappings")) {
                // 适配新结构: { "template_info": {...}, "mappings": [...] }
                List<SysTemplateMapping> dfMappings = result.getJSONArray("mappings")
                        .toJavaList(SysTemplateMapping.class);
                if (dfMappings != null) {
                    list.addAll(dfMappings);
                    log.info("智能识别完成，提取到 {} 个映射字段", dfMappings.size());
                }
                if (result.containsKey("template_info")) {
                    log.info("检测到模板信息: {}", result.getJSONObject("template_info").toJSONString());
                }
            } else {
                log.warn("Dify 返回结果不含 mappings: {}", result != null ? result.toJSONString() : "null");
                // 回退逻辑：如果 Dify 返回不规范或失败，进行基础解析
                log.warn("执行基础解析回退...");
                list.addAll(basicAnalyze(file));
            }
        } catch (Exception e) {
            log.error("智能分析过程中发生异常", e);
            list.addAll(basicAnalyze(file));
        } finally {
            // 清理临时文件
            if (StringUtils.isNotEmpty(tempPath)) {
                // Optional: delete or keep for audit
            }
        }
        log.info("analyzeDocument 准备返回，字段总数: {}", list.size());
        return list;
    }

    private List<SysTemplateMapping> basicAnalyze(MultipartFile file) {
        List<SysTemplateMapping> list = new ArrayList<>();
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream())) {
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
            log.error("基础分析失败", e);
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
        log.info("保存正式模版: {}", templateName);
        try (XWPFDocument doc = new XWPFDocument(file.getInputStream());
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            replaceText(doc, mappings);

            // 命名规则: LAB_yyyyMMdd_templateName.docx
            String fileName = "LAB_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "_" + templateName
                    + ".docx";
            String uploadPath = RuoYiConfig.getProfile() + "/templates/";
            File dir = new File(uploadPath);
            if (!dir.exists())
                dir.mkdirs();

            File targetFile = new File(uploadPath + fileName);
            try (FileOutputStream fos = new FileOutputStream(targetFile)) {
                doc.write(fos);
            }

            return Constants.RESOURCE_PREFIX + "/upload/templates/" + fileName;
        } catch (IOException e) {
            log.error("保存模版失败", e);
            throw new RuntimeException("保存模版失败");
        }
    }

    private JSONObject callDifyWorkflow(String localPath, String apiKey) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            // 1. 上传文件
            String uploadUrl = DIFY_BASE_URL + "/files/upload";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + apiKey);

            FileSystemResource fileResource = new FileSystemResource(localPath);
            MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
            body.add("file", fileResource);
            body.add("user", "lab-user");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful())
                return null;
            String uploadFileId = JSON.parseObject(response.getBody()).getString("id");

            // 2. 运行工作流
            String workflowUrl = DIFY_BASE_URL + "/workflows/run";
            HttpHeaders workflowHeaders = new HttpHeaders();
            workflowHeaders.setContentType(MediaType.APPLICATION_JSON);
            workflowHeaders.set("Authorization", "Bearer " + apiKey);

            JSONObject fileInput = new JSONObject();
            fileInput.put("type", "document");
            fileInput.put("transfer_method", "local_file");
            fileInput.put("upload_file_id", uploadFileId);

            JSONObject inputs = new JSONObject();
            inputs.put("file", fileInput);

            JSONObject workflowBody = new JSONObject();
            workflowBody.put("inputs", inputs);
            workflowBody.put("response_mode", "blocking");
            workflowBody.put("user", "lab-user");

            HttpEntity<String> workflowRequest = new HttpEntity<>(workflowBody.toJSONString(), workflowHeaders);
            ResponseEntity<String> workflowResponse = restTemplate.postForEntity(workflowUrl, workflowRequest,
                    String.class);

            if (!workflowResponse.getStatusCode().is2xxSuccessful())
                return null;

            // 3. 解析结果
            JSONObject workflowResult = JSON.parseObject(workflowResponse.getBody());
            JSONObject outputs = workflowResult.getJSONObject("data").getJSONObject("outputs");
            String rawText = outputs.getString("text");

            // 提取 JSON
            int start = rawText.indexOf("{");
            int end = rawText.lastIndexOf("}");
            if (start >= 0 && end > start) {
                return JSON.parseObject(rawText.substring(start, end + 1));
            }
        } catch (Exception e) {
            log.error("Dify 调用异常", e);
        }
        return null;
    }

    /**
     * v7：优化后的替换逻辑
     * 1. 分离 Dify 返回的"标题\n值"格式，只替换值部分
     * 2. 按长度倒序排列，防止短字串误伤长字串
     * 3. 单元格级聚合匹配，解决文本分片问题
     */
    private void replaceText(XWPFDocument doc, List<SysTemplateMapping> mappings) {
        if (mappings == null || mappings.isEmpty())
            return;

        // 按原文长度倒序排列
        mappings.sort((a, b) -> {
            int lenA = a.getOriginalText() == null ? 0 : a.getOriginalText().length();
            int lenB = b.getOriginalText() == null ? 0 : b.getOriginalText().length();
            return Integer.compare(lenB, lenA);
        });

        for (SysTemplateMapping m : mappings) {
            String rawTarget = m.getOriginalText();
            String placeholder = m.getPlaceholderKey();

            if (rawTarget == null || placeholder == null || rawTarget.trim().isEmpty()) {
                log.warn("跳过无效映射项: 原文={}, 变量={}", rawTarget, placeholder);
                continue;
            }

            // 修复四括号问题
            String replacement;
            if (placeholder.startsWith("{{") && placeholder.endsWith("}}")) {
                replacement = placeholder;
            } else {
                replacement = "{{" + placeholder + "}}";
            }

            // 生成候选目标列表（分离标题与值）
            java.util.List<String> targets = new java.util.ArrayList<>();
            if (rawTarget.contains("\n")) {
                String[] parts = rawTarget.split("\n", 2);
                if (parts.length == 2 && parts[1].trim().length() > 0) {
                    targets.add(parts[1].trim());
                }
            }
            targets.add(rawTarget.replaceAll("\\s+", " ").trim());
            targets.add(rawTarget);

            for (String target : targets) {
                if (target == null || target.trim().isEmpty())
                    continue;

                // 优先处理表格（单元格级聚合）
                for (XWPFTable tbl : doc.getTables()) {
                    replaceInTable(tbl, target, replacement);
                }

                // 段落级替换
                for (XWPFParagraph p : doc.getParagraphs()) {
                    replaceInParagraph(p, target, replacement);
                }

                // 页眉页脚
                for (XWPFHeader header : doc.getHeaderList()) {
                    for (XWPFParagraph p : header.getParagraphs()) {
                        replaceInParagraph(p, target, replacement);
                    }
                    for (XWPFTable tbl : header.getTables()) {
                        replaceInTable(tbl, target, replacement);
                    }
                }
                for (XWPFFooter footer : doc.getFooterList()) {
                    for (XWPFParagraph p : footer.getParagraphs()) {
                        replaceInParagraph(p, target, replacement);
                    }
                    for (XWPFTable tbl : footer.getTables()) {
                        replaceInTable(tbl, target, replacement);
                    }
                }
            }
        }
    }

    /**
     * v6 重写：单元格级「聚合-匹配-重写」策略
     * Word 会将文本碎片化存储在多个 Run 中，当前逻辑无法处理。
     * 新策略：聚合整个单元格文本，进行归一化匹配，匹配成功后重写整个单元格。
     */
    private void replaceInTable(XWPFTable tbl, String target, String replacement) {
        String normalizedTarget = target.replaceAll("\\s+", " ").trim();
        if (normalizedTarget.isEmpty())
            return;

        for (XWPFTableRow row : tbl.getRows()) {
            for (XWPFTableCell cell : row.getTableCells()) {
                String cellText = cell.getText();
                if (cellText == null || cellText.trim().isEmpty())
                    continue;

                String normalizedCellText = cellText.replaceAll("\\s+", " ").trim();

                // 核心逻辑：只要单元格文本包含目标文本，就执行替换
                if (normalizedCellText.contains(normalizedTarget)) {
                    // 执行替换：将目标文本替换为占位符，保留其余内容
                    String newCellText = normalizedCellText.replace(normalizedTarget, replacement);
                    clearCellAndSetText(cell, newCellText);
                    log.info("【单元格聚合替换】: '{}' -> '{}'", truncate(target, 40), replacement);
                }
            }
        }
    }

    private String truncate(String s, int max) {
        if (s == null)
            return "";
        return s.length() > max ? s.substring(0, max) + "..." : s;
    }

    private void clearCellAndSetText(XWPFTableCell cell, String text) {
        while (cell.getParagraphs().size() > 1) {
            cell.removeParagraph(0);
        }
        XWPFParagraph p = cell.getParagraphs().get(0);
        while (p.getRuns().size() > 0) {
            p.removeRun(0);
        }
        p.createRun().setText(text);
    }

    /**
     * 在段落中执行替换，支持跳过带图片的 Run，增加边界保护防止误杀
     */
    private void replaceInParagraph(XWPFParagraph p, String target, String replacement) {
        List<XWPFRun> runs = p.getRuns();
        if (runs == null || runs.isEmpty())
            return;

        String normalizedTarget = target.replaceAll("\\s+", " ").trim();
        if (normalizedTarget.isEmpty())
            return;

        // 1. 优先尝试在单个 Run 中直接替换（带边界保护）
        boolean substituted = false;
        for (XWPFRun r : runs) {
            if (r.getEmbeddedPictures() != null && !r.getEmbeddedPictures().isEmpty()) {
                continue;
            }

            String text = r.getText(0);
            if (text != null) {
                if (safeContains(text, target)) {
                    r.setText(safeReplace(text, target, replacement), 0);
                    substituted = true;
                } else {
                    String normalizedText = text.replaceAll("\\s+", " ");
                    if (safeContains(normalizedText, normalizedTarget)) {
                        r.setText(safeReplace(text, text.trim(), replacement), 0);
                        substituted = true;
                    }
                }
            }
        }

        // 2. 补偿逻辑：处理被 Word 拆分到多个 Run 中的情况
        if (!substituted) {
            String pText = p.getText();
            if (pText != null) {
                String normalizedPText = pText.replaceAll("\\s+", " ").trim();
                if (normalizedPText.contains(normalizedTarget)) {
                    XWPFRun firstTextRun = null;
                    for (XWPFRun r : runs) {
                        if (r.getEmbeddedPictures() == null || r.getEmbeddedPictures().isEmpty()) {
                            firstTextRun = r;
                            break;
                        }
                    }

                    if (firstTextRun != null) {
                        StringBuilder fullText = new StringBuilder();
                        for (XWPFRun r : runs) {
                            if (r.getEmbeddedPictures() == null || r.getEmbeddedPictures().isEmpty()) {
                                String t = r.getText(0);
                                fullText.append(t != null ? t : "");
                            }
                        }

                        String combined = fullText.toString();
                        String normalizedCombined = combined.replaceAll("\\s+", " ").trim();
                        if (normalizedCombined.contains(normalizedTarget)) {
                            // 使用归一化合并策略
                            String newFullText = normalizedCombined.replace(normalizedTarget, replacement);
                            firstTextRun.setText(newFullText, 0);

                            boolean firstFound = false;
                            for (XWPFRun r : runs) {
                                if (r.getEmbeddedPictures() == null || r.getEmbeddedPictures().isEmpty()) {
                                    if (!firstFound) {
                                        firstFound = true;
                                    } else {
                                        r.setText("", 0);
                                    }
                                }
                            }
                            log.info("已通过合并策略处理分段文本: '{}' -> '{}'", target, replacement);
                        }
                    }
                }
            }
        }
    }

    /**
     * 安全检查：如果目标是短词，检查其前后是否为字母数字，防止 mis-match (如 ZONE 中的 ONE)
     */
    private boolean safeContains(String text, String target) {
        if (!text.contains(target))
            return false;
        // 如果 target 包含非字母数字（如空格、逗号），通常认为是安全的短语匹配
        if (!target.matches("^[a-zA-Z0-9]+$"))
            return true;

        // 词边界检查逻辑
        int index = text.indexOf(target);
        while (index != -1) {
            boolean startOk = (index == 0) || !Character.isLetterOrDigit(text.charAt(index - 1));
            boolean endOk = (index + target.length() == text.length())
                    || !Character.isLetterOrDigit(text.charAt(index + target.length()));

            if (startOk && endOk)
                return true;
            index = text.indexOf(target, index + 1);
        }
        return false;
    }

    private String safeReplace(String text, String target, String replacement) {
        if (!target.matches("^[a-zA-Z0-9]+$"))
            return text.replace(target, replacement);

        // 使用正则词边界进行精确替换
        String patternString = "(?<![a-zA-Z0-9])" + java.util.regex.Pattern.quote(target) + "(?![a-zA-Z0-9])";
        return text.replaceAll(patternString, replacement);
    }
}

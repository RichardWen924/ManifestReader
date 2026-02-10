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

    private void replaceText(XWPFDocument doc, List<SysTemplateMapping> mappings) {
        if (mappings == null || mappings.isEmpty())
            return;

        // 1. 过滤无效映射，并按原文长度倒序排列
        List<SysTemplateMapping> validMappings = new ArrayList<>();
        for (SysTemplateMapping m : mappings) {
            if (m != null && StringUtils.isNotEmpty(m.getOriginalText())
                    && StringUtils.isNotEmpty(m.getPlaceholderKey())) {
                validMappings.add(m);
            }
        }
        validMappings.sort((a, b) -> b.getOriginalText().length() - a.getOriginalText().length());

        // 2. 核心：以单元格 (Cell) 为单位处理表格
        // 每个单元格内的段落列表可以视为一个有序的"上下文块"
        // 在这个块内，同一个映射只会被应用一次（消歧）
        java.util.Set<String> processedCellIds = new java.util.HashSet<>();

        for (XWPFTable tbl : doc.getTables()) {
            for (XWPFTableRow row : tbl.getRows()) {
                for (XWPFTableCell cell : row.getTableCells()) {
                    // 用 cell 的 hashCode 去重（合并单元格会产生重复引用）
                    String cellId = String.valueOf(System.identityHashCode(cell));
                    if (processedCellIds.contains(cellId))
                        continue;
                    processedCellIds.add(cellId);

                    processCell(cell, validMappings);
                }
            }
        }

        // 3. 处理正文段落（非表格部分）
        for (XWPFParagraph p : doc.getParagraphs()) {
            replaceInParagraphSingle(p, validMappings);
        }

        // 4. 处理页眉/页脚/脚注/尾注
        for (XWPFHeader header : doc.getHeaderList()) {
            for (XWPFParagraph p : header.getParagraphs()) {
                replaceInParagraphSingle(p, validMappings);
            }
        }
        for (XWPFFooter footer : doc.getFooterList()) {
            for (XWPFParagraph p : footer.getParagraphs()) {
                replaceInParagraphSingle(p, validMappings);
            }
        }

        log.info("文档全域变量替换工作完成。共处理 {} 个有效映射。", validMappings.size());
    }

    /**
     * 以单元格为单位处理所有映射。
     * 核心逻辑：将多行 original_text 拆分后，在单元格的段落列表中查找连续匹配。
     * 每个段落只会被一个映射"认领"，已认领的段落不会被后续映射覆盖（歧义消解）。
     */
    private void processCell(XWPFTableCell cell, List<SysTemplateMapping> mappings) {
        List<XWPFParagraph> paragraphs = cell.getParagraphs();
        if (paragraphs == null || paragraphs.isEmpty())
            return;

        // 记录哪些段落已被认领（段落索引 -> 已认领）
        java.util.Set<Integer> claimedParagraphs = new java.util.HashSet<>();

        for (SysTemplateMapping m : mappings) {
            String originalText = m.getOriginalText();
            String placeholder = "{{" + m.getPlaceholderKey() + "}}";

            // 将 original_text 按换行拆分为多行
            String[] lines = originalText.split("\\n");
            // 去掉空行
            List<String> targetLines = new ArrayList<>();
            for (String line : lines) {
                String trimmed = line.trim();
                if (!trimmed.isEmpty()) {
                    targetLines.add(trimmed);
                }
            }
            if (targetLines.isEmpty())
                continue;

            // 在段落列表中查找首行匹配
            String firstLine = targetLines.get(0);

            for (int pi = 0; pi < paragraphs.size(); pi++) {
                if (claimedParagraphs.contains(pi))
                    continue;

                String pText = paragraphs.get(pi).getText();
                if (StringUtils.isEmpty(pText))
                    continue;

                // 判断是否匹配首行
                if (!pText.trim().equals(firstLine) && !pText.contains(firstLine))
                    continue;

                // 如果是多行映射，需要连续段落匹配
                if (targetLines.size() > 1) {
                    boolean allMatch = true;
                    for (int offset = 1; offset < targetLines.size() && (pi + offset) < paragraphs.size(); offset++) {
                        String nextPText = paragraphs.get(pi + offset).getText();
                        if (nextPText == null || !nextPText.trim().equals(targetLines.get(offset))) {
                            allMatch = false;
                            break;
                        }
                    }
                    if (!allMatch)
                        continue;

                    // 多行匹配成功：将第一行替换为占位符，后续行清空
                    if (pText.trim().equals(firstLine)) {
                        // 整段就是首行 -> 直接替换整段
                        clearAndSetParagraph(paragraphs.get(pi), placeholder);
                    } else {
                        // 首行嵌入在段落中 -> 手术刀替换
                        performSurgicalReplace(paragraphs.get(pi), firstLine, placeholder);
                    }
                    claimedParagraphs.add(pi);

                    for (int offset = 1; offset < targetLines.size() && (pi + offset) < paragraphs.size(); offset++) {
                        clearAndSetParagraph(paragraphs.get(pi + offset), "");
                        claimedParagraphs.add(pi + offset);
                    }
                    log.info("多行映射替换成功: {} -> {}", m.getPlaceholderKey(), placeholder);
                    break; // 当前映射只认领一次

                } else {
                    // 单行映射
                    if (pText.trim().equals(firstLine)) {
                        // 整段精确匹配
                        clearAndSetParagraph(paragraphs.get(pi), placeholder);
                        claimedParagraphs.add(pi);
                        log.info("单行精确替换: {} -> {}", firstLine, placeholder);
                        break;
                    } else if (pText.contains(firstLine)) {
                        // 子串匹配（如 "8492KGS 68CBM" 中匹配 "8492"）
                        performSurgicalReplace(paragraphs.get(pi), firstLine, placeholder);
                        // 子串替换不认领整个段落（同一段落可能包含多个字段）
                        log.info("子串替换: {} in '{}' -> {}", firstLine, pText, placeholder);
                        break;
                    }
                }
            }
        }

        // 递归处理嵌套表格
        for (XWPFTable nestedTable : cell.getTables()) {
            for (XWPFTableRow row : nestedTable.getRows()) {
                for (XWPFTableCell nestedCell : row.getTableCells()) {
                    processCell(nestedCell, mappings);
                }
            }
        }
    }

    /**
     * 清空段落所有 Run 并设置新文本（保留第一个 Run 的样式）
     */
    private void clearAndSetParagraph(XWPFParagraph p, String newText) {
        List<XWPFRun> runs = p.getRuns();
        if (runs == null || runs.isEmpty()) {
            if (StringUtils.isNotEmpty(newText)) {
                XWPFRun run = p.createRun();
                run.setText(newText, 0);
            }
            return;
        }

        // 保留第一个 Run 的样式，设置新文本
        runs.get(0).setText(newText, 0);

        // 移除其余所有 Run
        for (int i = runs.size() - 1; i > 0; i--) {
            p.removeRun(i);
        }
    }

    /**
     * 在正文段落中做简单的单行替换（非单元格场景）
     */
    private void replaceInParagraphSingle(XWPFParagraph p, List<SysTemplateMapping> mappings) {
        String pText = p.getText();
        if (StringUtils.isEmpty(pText))
            return;

        for (SysTemplateMapping m : mappings) {
            String target = m.getOriginalText();
            if (StringUtils.isEmpty(target))
                continue;

            // 只匹配非多行的情况
            String firstLine = target.split("\\n")[0].trim();
            if (pText.contains(firstLine)) {
                performSurgicalReplace(p, firstLine, "{{" + m.getPlaceholderKey() + "}}");
                pText = p.getText();
            }
        }
    }

    /**
     * 对段落执行"手术刀"替换逻辑（保留格式的精准子串替换）
     */
    private boolean performSurgicalReplace(XWPFParagraph p, String target, String replacement) {
        List<XWPFRun> runs = p.getRuns();
        if (runs == null || runs.isEmpty())
            return false;

        StringBuilder fullText = new StringBuilder();
        List<Integer> runStarts = new ArrayList<>();

        // 1. 构建全文本坐标轴
        for (XWPFRun r : runs) {
            runStarts.add(fullText.length());
            String t = r.getText(0);
            fullText.append(t != null ? t : "");
        }

        String content = fullText.toString();
        int matchIndex = content.indexOf(target);
        if (matchIndex == -1)
            return false;

        int matchEnd = matchIndex + target.length();

        // 2. 找到起始 Run 和结束 Run 的索引
        int startRunIdx = -1;
        int endRunIdx = -1;

        for (int i = 0; i < runStarts.size(); i++) {
            int start = runStarts.get(i);
            int nextStart = (i + 1 < runStarts.size()) ? runStarts.get(i + 1) : content.length();

            if (matchIndex >= start && matchIndex < nextStart) {
                startRunIdx = i;
            }
            if (matchEnd > start && matchEnd <= nextStart) {
                endRunIdx = i;
            }
        }

        if (startRunIdx == -1 || endRunIdx == -1)
            return false;

        // 3. 执行"缝合"操作
        XWPFRun startRun = runs.get(startRunIdx);
        String startText = startRun.getText(0);
        if (startText == null)
            return false;
        int offsetInStart = matchIndex - runStarts.get(startRunIdx);

        // A. 在起始 Run 注入占位符
        String prefix = startText.substring(0, offsetInStart);
        String suffix = (startRunIdx == endRunIdx) ? startText.substring(offsetInStart + target.length()) : "";
        startRun.setText(prefix + replacement + suffix, 0);

        // B. 处理中间及末尾的 Run
        if (startRunIdx != endRunIdx) {
            // 清理末尾 Run 的受影响部分
            XWPFRun endRun = runs.get(endRunIdx);
            String endText = endRun.getText(0);
            if (endText != null) {
                int offsetInEnd = matchEnd - runStarts.get(endRunIdx);
                endRun.setText(endText.substring(offsetInEnd), 0);
            }

            // 彻底移除中间的文本 Run
            for (int i = endRunIdx - 1; i > startRunIdx; i--) {
                p.removeRun(i);
            }
        }

        return true;
    }
}

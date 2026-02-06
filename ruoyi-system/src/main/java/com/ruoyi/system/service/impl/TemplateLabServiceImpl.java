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
        for (SysTemplateMapping m : mappings) {
            String target = m.getOriginalText();
            String placeholder = m.getPlaceholderKey();

            if (target == null || placeholder == null || target.isEmpty()) {
                log.warn("跳过无效映射项: 原文={}, 变量={}", target, placeholder);
                continue;
            }

            String replacement = "{{" + placeholder + "}}";

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

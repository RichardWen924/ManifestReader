package com.ruoyi.system.service.impl;

import java.io.File;
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
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.beans.factory.annotation.Autowired;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import com.ruoyi.system.domain.SysPdfTemplate;
import com.ruoyi.system.service.ISysPdfTemplateService;

@Service
public class TemplateLabServiceImpl implements ITemplateLabService {
    private static final Logger log = LoggerFactory.getLogger(TemplateLabServiceImpl.class);

    @Autowired
    private ISysPdfTemplateService pdfTemplateService;

    private static final String DIFY_API_KEY_ANALYZE = "app-1EVrJL9CVPCYVkS2TOs9XC3A";
    private static final String DIFY_BASE_URL = "http://localhost/v1";

    @Autowired
    private com.ruoyi.system.service.ISysConfigService configService;

    @Override
    public List<SysTemplateMapping> analyzeDocument(MultipartFile file) {
        log.info("开始智能分析模版文档: {}", file.getOriginalFilename());
        List<SysTemplateMapping> list = new ArrayList<>();

        // 检查配置，判断是否开启测试模式 (默认为 false，即正常模式)
        boolean isTestMode = false;
        try {
            String config = configService.selectConfigByKey("sys.dify.test.enabled");
            isTestMode = Boolean.parseBoolean(config);
        } catch (Exception e) {
            log.warn("获取 sys.dify.test.enabled 配置失败，默认关闭测试模式");
        }

        if (isTestMode) {
            // ---------------------------------------------------------
            // 测试模式：使用静态数据
            // ---------------------------------------------------------
            String testData = "{\"mappings\":[{\"original_text\":\"ZIMUSHH32021612\",\"placeholder_key\":\"doc_no\",\"data_type\":\"string\",\"description\":\"文档编号\"},{\"original_text\":\"EVHL26020240\",\"placeholder_key\":\"bl_no\",\"data_type\":\"string\",\"description\":\"提单号\"},{\"original_text\":null,\"placeholder_key\":\"booking_no\",\"data_type\":\"string\",\"description\":\"预订号\"},{\"original_text\":\"CHENGDU LIYUXIN TRADING CO., LTD 1ST FLOOR, NO.16 ZHONGHE XIONGJIAQIAO ROAD, CHENGDU HIGH TECH ZONE\",\"placeholder_key\":\"shipper\",\"data_type\":\"string\",\"description\":\"托运人\"},{\"original_text\":\"NEWSTAR TECHNOLOGY INC 645 GATES AVE STE 112, BROOKLYN, NY, 11221, USA\",\"placeholder_key\":\"consignee\",\"data_type\":\"string\",\"description\":\"收货人\"},{\"original_text\":\"NEWSTAR TECHNOLOGY INC 645 GATES AVE STE 112, BROOKLYN, NY, 11221, USA SEA-US.OP@YPLOGISTICS.COM\",\"placeholder_key\":\"notify_party\",\"data_type\":\"string\",\"description\":\"通知方\"},{\"original_text\":\"EURO-AMERICA CONTAINER LINE INC 1475 S. STATE COLLEGE BLVD. #120 ANAHEIM, CA 92806 OP@EUROAMERICA-USA.COM DOC@EUROAMERICA-USA.COM TEL: 1-657-655-6228\",\"placeholder_key\":\"delivery_agent\",\"data_type\":\"string\",\"description\":\"交付代理\"},{\"original_text\":\"EVERSTAR (GUANGDONG) SUPPLY CHAIN TECHNOLOGY CO. , LTD\",\"placeholder_key\":\"carrier_agent\",\"data_type\":\"string\",\"description\":\"承运人代理\"},{\"original_text\":\"ZIM SCORPIO 8E\",\"placeholder_key\":\"vessel_voyage\",\"data_type\":\"string\",\"description\":\"船舶/航次\"},{\"original_text\":\"YANTIAN\",\"placeholder_key\":\"port_of_loading\",\"data_type\":\"string\",\"description\":\"装货港\"},{\"original_text\":\"NEW YORK,NY\",\"placeholder_key\":\"port_of_discharge\",\"data_type\":\"string\",\"description\":\"卸货港\"},{\"original_text\":\"YANTIAN\",\"placeholder_key\":\"place_of_receipt\",\"data_type\":\"string\",\"description\":\"收货地点\"},{\"original_text\":\"NEW YORK,NY\",\"placeholder_key\":\"place_of_delivery\",\"data_type\":\"string\",\"description\":\"交付地点\"},{\"original_text\":\"YANTIAN\",\"placeholder_key\":\"pre_carriage_by\",\"data_type\":\"string\",\"description\":\"预装运方式\"},{\"original_text\":\"ZCSU7894848\",\"placeholder_key\":\"container_no\",\"data_type\":\"string\",\"description\":\"集装箱号\"},{\"original_text\":null,\"placeholder_key\":\"seal_no\",\"data_type\":\"string\",\"description\":\"封条号\"},{\"original_text\":\"N/M\",\"placeholder_key\":\"marks\",\"data_type\":\"string\",\"description\":\"标记\"},{\"original_text\":\"PLASTIC PHOTO FRAME\\nPLASTIC SCREEN PROTECTOR\",\"placeholder_key\":\"goods_description\",\"data_type\":\"string\",\"description\":\"货物描述\"},{\"original_text\":\"737\",\"placeholder_key\":\"package_quantity\",\"data_type\":\"string\",\"description\":\"包装数量\"},{\"original_text\":\"CARTONS\",\"placeholder_key\":\"package_unit\",\"data_type\":\"string\",\"description\":\"包装单位\"},{\"original_text\":\"8492\",\"placeholder_key\":\"gross_weight_kgs\",\"data_type\":\"string\",\"description\":\"毛重(KGS)\"},{\"original_text\":\"68\",\"placeholder_key\":\"measurement_cbm\",\"data_type\":\"string\",\"description\":\"体积(CBM)\"},{\"original_text\":null,\"placeholder_key\":\"container_weight\",\"data_type\":\"string\",\"description\":\"集装箱重量\"},{\"original_text\":null,\"placeholder_key\":\"vgm_weight\",\"data_type\":\"string\",\"description\":\"VGM重量\"},{\"original_text\":null,\"placeholder_key\":\"serial_no\",\"data_type\":\"string\",\"description\":\"序列号\"},{\"original_text\":\"CY/CY O/O\",\"placeholder_key\":\"service_type\",\"data_type\":\"string\",\"description\":\"服务类型\"},{\"original_text\":null,\"placeholder_key\":\"service_mode\",\"data_type\":\"string\",\"description\":\"服务模式\"},{\"original_text\":\"PREPAID\",\"placeholder_key\":\"freight_term\",\"data_type\":\"string\",\"description\":\"运费条款\"},{\"original_text\":\"AS ARRANGED\",\"placeholder_key\":\"collect_amount\",\"data_type\":\"string\",\"description\":\"收款金额\"},{\"original_text\":\"PREPAID\",\"placeholder_key\":\"prepaid_amount\",\"data_type\":\"string\",\"description\":\"预付金额\"},{\"original_text\":null,\"placeholder_key\":\"revenue_tons\",\"data_type\":\"string\",\"description\":\"收入吨数\"},{\"original_text\":\"YANTIAN\",\"placeholder_key\":\"payable_at\",\"data_type\":\"string\",\"description\":\"付款地点\"},{\"original_text\":\"ONE(1)\",\"placeholder_key\":\"original_bl_count\",\"data_type\":\"string\",\"description\":\"原始提单数量\"},{\"original_text\":\"YANTIAN\",\"placeholder_key\":\"issue_place\",\"data_type\":\"string\",\"description\":\"开证地点\"},{\"original_text\":null,\"placeholder_key\":\"laden_on_board\",\"data_type\":\"string\",\"description\":\"装载日期\"}]}";
            JSONObject resultJSON = JSON.parseObject(testData);
            if (resultJSON != null && resultJSON.containsKey("mappings")) {
                List<SysTemplateMapping> dfMappings = resultJSON.getJSONArray("mappings")
                        .toJavaList(SysTemplateMapping.class);
                if (dfMappings != null) {
                    list.addAll(dfMappings);
                    log.info("【测试模式】提取到 {} 个模拟映射字段", dfMappings.size());
                }
            }
        } else {
            // ---------------------------------------------------------
            // 正常模式：调用 Dify 工作流
            // ---------------------------------------------------------
            String tempPath = "";
            try {
                tempPath = FileUploadUtils.upload(RuoYiConfig.getProfile(), file);
                String fullPath = RuoYiConfig.getProfile() +
                        tempPath.replaceFirst(Constants.RESOURCE_PREFIX, "");

                JSONObject result = callDifyWorkflow(fullPath, DIFY_API_KEY_ANALYZE);

                if (result != null && result.containsKey("mappings")) {
                    List<SysTemplateMapping> dfMappings = result.getJSONArray("mappings")
                            .toJavaList(SysTemplateMapping.class);
                    if (dfMappings != null) {
                        list.addAll(dfMappings);
                        log.info("智能识别完成，提取到 {} 个映射字段", dfMappings.size());
                    }
                    if (result.containsKey("template_info")) {
                        log.info("检测到模板信息: {}",
                                result.getJSONObject("template_info").toJSONString());
                    }
                } else {
                    log.warn("Dify 返回结果不含 mappings: {}", result != null ? result.toJSONString() : "null");
                    log.warn("执行基础解析回退...");
                    list.addAll(basicAnalyze(file));
                }
            } catch (Exception e) {
                log.error("智能分析过程中发生异常", e);
                list.addAll(basicAnalyze(file));
            } finally {
                if (StringUtils.isNotEmpty(tempPath)) {
                    try {
                        String fullPath = RuoYiConfig.getProfile()
                                + tempPath.replaceFirst(Constants.RESOURCE_PREFIX, "");
                        File fileToDelete = new File(fullPath);
                        if (fileToDelete.exists()) {
                            boolean deleted = fileToDelete.delete();
                            log.info("智能分析完成，立即清理临时文件: {}, 结果: {}", fullPath, deleted);
                        }
                    } catch (Exception e) {
                        log.warn("清理临时文件失败: {}", tempPath);
                    }
                }
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
        try {
            // 1. 保存上传文件到临时路径
            String tempDir = System.getProperty("java.io.tmpdir");
            String inputPath = tempDir + "/tpl_preview_input_" + System.currentTimeMillis() + ".docx";
            String outputPath = tempDir + "/tpl_preview_output_" + System.currentTimeMillis() + ".docx";
            file.transferTo(new File(inputPath));

            // 2. 调用 Python 替换引擎
            callPythonReplace(inputPath, outputPath, mappings);

            // 3. 读取结果
            File outputFile = new File(outputPath);
            if (outputFile.exists()) {
                byte[] result = java.nio.file.Files.readAllBytes(outputFile.toPath());
                // 清理临时文件
                new File(inputPath).delete();
                outputFile.delete();
                return result;
            }
            log.error("Python 替换引擎未生成输出文件");
            return new byte[0];
        } catch (Exception e) {
            log.error("预览模版失败", e);
            return new byte[0];
        }
    }

    @Override
    public String saveTemplate(MultipartFile file, List<SysTemplateMapping> mappings, String templateName) {
        log.info("保存正式模版: {}", templateName);
        try {
            // 1. 保存上传文件到临时路径
            String tempDir = System.getProperty("java.io.tmpdir");
            String inputPath = tempDir + "/tpl_save_input_" + System.currentTimeMillis() + ".docx";
            file.transferTo(new File(inputPath));

            // 2. 命名输出文件
            String fileName = "LAB_" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "_" + templateName
                    + ".docx";
            String uploadPath = RuoYiConfig.getProfile() + "/templates/";
            File dir = new File(uploadPath);
            if (!dir.exists())
                dir.mkdirs();
            String outputPath = uploadPath + fileName;

            // 3. 调用 Python 替换引擎
            callPythonReplace(inputPath, outputPath, mappings);

            // 4. 清理临时文件
            new File(inputPath).delete();

            // 5. 验证输出
            if (!new File(outputPath).exists()) {
                throw new RuntimeException("Python 替换引擎未生成输出文件");
            }

            return Constants.RESOURCE_PREFIX + "/upload/templates/" + fileName;
        } catch (Exception e) {
            log.error("保存模版失败", e);
            throw new RuntimeException("保存模版失败: " + e.getMessage());
        }
    }

    /**
     * 调用 Python 替换引擎 (python-docx)
     */
    private void callPythonReplace(String inputPath, String outputPath, List<SysTemplateMapping> mappings)
            throws Exception {
        // 1. 将 mappings 写入临时 JSON 文件
        String tempDir = System.getProperty("java.io.tmpdir");
        String mappingsPath = tempDir + "/tpl_mappings_" + System.currentTimeMillis() + ".json";
        String mappingsJson = JSON.toJSONString(mappings);
        java.nio.file.Files.write(java.nio.file.Paths.get(mappingsPath), mappingsJson.getBytes("UTF-8"));

        // 2. 构建 Python 脚本路径（相对于项目根目录）
        String scriptPath = System.getProperty("user.dir") + "/scripts/template_replace.py";
        // 如果脚本不在默认位置，尝试其他常见位置
        if (!new File(scriptPath).exists()) {
            // 尝试从配置的上传路径推断项目路径
            String altPath = RuoYiConfig.getProfile().replace("/uploadPath", "") + "/scripts/template_replace.py";
            if (new File(altPath).exists()) {
                scriptPath = altPath;
            }
        }

        log.info("调用 Python 替换引擎: python3 {} {} {} {}", scriptPath, inputPath, outputPath, mappingsPath);

        // 3. 执行 Python 脚本
        ProcessBuilder pb = new ProcessBuilder("python3", scriptPath, inputPath, outputPath, mappingsPath);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        // 读取输出
        // 读取输出 (Java 8 兼容写法)
        java.io.InputStream is = process.getInputStream();
        java.io.ByteArrayOutputStream buffer = new java.io.ByteArrayOutputStream();
        int nRead;
        byte[] data = new byte[1024];
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        String output = buffer.toString("UTF-8");
        int exitCode = process.waitFor();

        // 清理 mappings 临时文件
        new File(mappingsPath).delete();

        if (exitCode != 0) {
            log.error("Python 替换引擎执行失败 (exit={}): {}", exitCode, output);
            throw new RuntimeException("Python 替换引擎失败: " + output);
        }

        log.info("Python 替换引擎完成: {}", output);
    }

    private JSONObject callDifyWorkflow(String localPath, String apiKey) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(300000); // 300秒连接超时
        factory.setReadTimeout(300000); // 300秒读取超时
        RestTemplate restTemplate = new RestTemplate(factory);
        try {
            // 1. 上传文件
            String uploadUrl = DIFY_BASE_URL + "/files/upload";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + apiKey);

            File file = new File(localPath);
            if (!file.exists()) {
                log.error("文件不存在: {}", localPath);
                return null;
            }

            // 使用 ByteArrayResource 并强制指定文件名，避免路径问题和中文文件名问题
            byte[] fileContent = java.nio.file.Files.readAllBytes(file.toPath());
            String originalFilename = file.getName();
            String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            String safeFilename = java.util.UUID.randomUUID().toString() + extension;

            ByteArrayResource fileResource = new ByteArrayResource(fileContent) {
                @Override
                public String getFilename() {
                    return safeFilename;
                }
            };

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
        } catch (HttpStatusCodeException e) {
            log.error("Dify 调用异常 (HTTP {}): {}", e.getStatusCode(), e.getResponseBodyAsString(), e);
        } catch (Exception e) {
            log.error("Dify 调用异常", e);
        }
        return null;
    }

    @Override
    public byte[] exportWithTemplate(Long templateId, Map<String, Object> businessData) {
        log.info("使用模版导出: templateId={}", templateId);
        try {
            // 1. 从数据库读取模版记录
            SysPdfTemplate template = pdfTemplateService.selectSysPdfTemplateById(templateId);
            if (template == null) {
                throw new RuntimeException("模版不存在: " + templateId);
            }

            // 2. 解析模版文件真实路径
            // DB 存储格式: /profile/upload/templates/LAB_xxx.docx
            // 真实路径: RuoYiConfig.getProfile() + "/templates/" + fileName
            String dbPath = template.getTemplateFilePath();
            String fileName = dbPath.substring(dbPath.lastIndexOf("/") + 1);
            String realPath = RuoYiConfig.getProfile() + "/templates/" + fileName;

            File templateFile = new File(realPath);
            if (!templateFile.exists()) {
                throw new RuntimeException("模版文件不存在: " + realPath);
            }

            // 3. 解析 fieldConfig 得到 placeholder_key 列表
            // fieldConfig 格式:
            // [{"placeholder_key":"shipper","original_text":"...","data_type":"string",...},
            // ...]
            String fieldConfigJson = template.getFieldConfig();
            List<SysTemplateMapping> mappings = new ArrayList<>();
            if (StringUtils.isNotEmpty(fieldConfigJson)) {
                mappings = JSON.parseArray(fieldConfigJson, SysTemplateMapping.class);
            }

            // 4. 打开模版 docx，将 {{placeholder_key}} 替换为 businessData 中对应的值
            java.io.FileInputStream fis = new java.io.FileInputStream(templateFile);
            XWPFDocument doc = new XWPFDocument(fis);
            fis.close();

            for (SysTemplateMapping m : mappings) {
                String placeholder = "{{" + m.getPlaceholderKey() + "}}";
                String value = "";
                if (businessData != null && businessData.containsKey(m.getPlaceholderKey())) {
                    Object v = businessData.get(m.getPlaceholderKey());
                    value = v != null ? v.toString() : "";
                }
                // 替换段落
                for (XWPFParagraph p : doc.getParagraphs()) {
                    replaceInParagraph(p, placeholder, value);
                }
                // 替换表格中的段落
                for (XWPFTable table : doc.getTables()) {
                    for (XWPFTableRow row : table.getRows()) {
                        for (XWPFTableCell cell : row.getTableCells()) {
                            for (XWPFParagraph p : cell.getParagraphs()) {
                                replaceInParagraph(p, placeholder, value);
                            }
                        }
                    }
                }
            }

            // 5. 输出为 byte[]
            java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
            doc.write(bos);
            doc.close();
            return bos.toByteArray();

        } catch (Exception e) {
            log.error("模版导出失败", e);
            throw new RuntimeException("模版导出失败: " + e.getMessage());
        }
    }

    /**
     * 在段落中替换占位符文本
     */
    private void replaceInParagraph(XWPFParagraph paragraph, String placeholder, String value) {
        String text = paragraph.getText();
        if (text == null || !text.contains(placeholder))
            return;

        // 尝试在单个 run 中替换
        for (XWPFRun run : paragraph.getRuns()) {
            String runText = run.getText(0);
            if (runText != null && runText.contains(placeholder)) {
                run.setText(runText.replace(placeholder, value), 0);
            }
        }

        // 如果占位符跨越多个 run，合并后替换
        String afterText = paragraph.getText();
        if (afterText != null && afterText.contains(placeholder)) {
            // 合并所有 run 文本
            StringBuilder sb = new StringBuilder();
            for (XWPFRun run : paragraph.getRuns()) {
                String rt = run.getText(0);
                if (rt != null)
                    sb.append(rt);
            }
            String merged = sb.toString().replace(placeholder, value);
            // 清除旧 run 文本
            List<XWPFRun> runs = paragraph.getRuns();
            for (int i = 0; i < runs.size(); i++) {
                if (i == 0) {
                    runs.get(i).setText(merged, 0);
                } else {
                    runs.get(i).setText("", 0);
                }
            }
        }
    }

}

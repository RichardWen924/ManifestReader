package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.system.mapper.BookingConsolidatedMapper;
import com.ruoyi.system.domain.BookingConsolidated;
import com.ruoyi.system.domain.BookingConsolidatedDto;
import com.ruoyi.system.domain.BookingConsolidatedDto.FieldLocation;
import com.ruoyi.system.service.IBookingConsolidatedService;
import com.ruoyi.system.utils.PdfEditUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.client.RestTemplate;

/**
 * 订舱与集装箱合并信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
@Service
public class BookingConsolidatedServiceImpl implements IBookingConsolidatedService {
    private static final Logger log = LoggerFactory.getLogger(BookingConsolidatedServiceImpl.class);

    private static final String DIFY_API_KEY_ANALYZE = "app-qFk49MpWcQKiqY41Q7IdDwIj";
    private static final String DIFY_API_KEY_GENERATE_HTML = "app-ZMAt882RoK60mAH2MvgjNEub";
    private static final String DIFY_BASE_URL = "http://localhost/v1";
    private static final String REDIS_PREFIX = "pdf_edit:";
    private static final String DEFAULT_TEMPLATE_CODE = "booking_standard";

    @Autowired
    private BookingConsolidatedMapper bookingConsolidatedMapper;

    @Autowired
    private com.ruoyi.system.mapper.BillOfLadingMapper billOfLadingMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private com.ruoyi.system.mapper.SysPdfTemplateMapper sysPdfTemplateMapper;

    @Override
    public BookingConsolidated selectBookingConsolidatedByBookingNo(String bookingNo) {
        return bookingConsolidatedMapper.selectBookingConsolidatedByBookingNo(bookingNo);
    }

    @Override
    public List<BookingConsolidated> selectBookingConsolidatedList(BookingConsolidated bookingConsolidated) {
        return bookingConsolidatedMapper.selectBookingConsolidatedList(bookingConsolidated);
    }

    @Override
    public int insertBookingConsolidated(BookingConsolidated bookingConsolidated) {
        return bookingConsolidatedMapper.insertBookingConsolidated(bookingConsolidated);
    }

    @Override
    public int updateBookingConsolidated(BookingConsolidated bookingConsolidated) {
        return bookingConsolidatedMapper.updateBookingConsolidated(bookingConsolidated);
    }

    @Override
    public int deleteBookingConsolidatedByBookingNos(String ids) {
        // 将逗号分隔的id字符串转换为Long数组
        String[] idStrArray = Convert.toStrArray(ids);
        Long[] idArray = new Long[idStrArray.length];
        for (int i = 0; i < idStrArray.length; i++) {
            idArray[i] = Long.parseLong(idStrArray[i]);
        }
        return bookingConsolidatedMapper.deleteBookingConsolidatedByBookingNos(idArray);
    }

    @Override
    public int deleteBookingConsolidatedByBookingNo(Long id) {
        return bookingConsolidatedMapper.deleteBookingConsolidatedByBookingNo(id);
    }

    /**
     * 1. 直接保存模式（不调用Dify，接受PDF编辑后的数据直接保存）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingConsolidated directProcessAndSave(String filePath, Map<String, Object> editedData) {
        log.info("直接保存PDF编辑后的数据，文件路径: {}", filePath);

        // 应用业务规则（blNo/bookingNo同步、运费逻辑等）
        log.info("应用业务规则...");
        com.ruoyi.system.utils.BillOfLadingValidator.applyBusinessRules(editedData);
        log.info("业务规则应用完成");

        // 转换字段名：驼峰 -> 下划线（用于数据库保存）
        Map<String, Object> dbData = convertCamelToUnderscore(editedData);
        log.info("转换后的数据（数据库格式）: {}", dbData);

        // 确保 booking_no 存在
        if (StringUtils.isEmpty((String) dbData.get("booking_no"))) {
            String generatedBookingNo = "BK" + System.currentTimeMillis();
            dbData.put("booking_no", generatedBookingNo);
            log.warn("booking_no 不存在，生成默认值: {}", generatedBookingNo);
        }

        // 保存到数据库（使用新的bill_of_lading表）
        com.ruoyi.system.domain.BillOfLading bl = mapMapToBillOfLading(dbData);

        // 确保bl_no存在
        if (StringUtils.isEmpty(bl.getBlNo())) {
            bl.setBlNo("BL" + System.currentTimeMillis());
        }

        // 设置PDF文件路径
        if (!StringUtils.isEmpty(filePath)) {
            bl.setFilePath(filePath);
        }

        log.info("准备插入bill_of_lading_v3表，bl_no: {}, booking_no: {}, 文件路径: {}",
                bl.getBlNo(), bl.getBookingNo(), filePath);
        billOfLadingMapper.insertBillOfLading(bl);

        // 返回结果（为了兼容性）
        BookingConsolidated bc = new BookingConsolidated();
        bc.setBookingNo(bl.getBookingNo());
        bc.setFilePath(filePath);
        return bc;
    }

    /**
     * 2. AI智能提取 - 分析文件：上传 -> 识别 -> 缓存 -> 返回DTO (供前端确认)
     */
    @Override
    public BookingConsolidatedDto analyzeFile(String filePath) {
        log.info("开始分析文件: {}", filePath);

        // 调用 Dify 获取 JSON 数据 (使用分析模式的 API Key)
        JSONObject dataJson = callDifyWorkflow(filePath, DIFY_API_KEY_ANALYZE);
        if (dataJson == null) {
            throw new RuntimeException("Dify 识别失败");
        }
        log.info("Dify 识别结果: {}", dataJson.toJSONString());

        // 从数据库加载 PDF 模版配置
        com.ruoyi.system.domain.SysPdfTemplate template = sysPdfTemplateMapper
                .selectSysPdfTemplateByCode(DEFAULT_TEMPLATE_CODE);
        if (template == null) {
            throw new RuntimeException("PDF 模版配置不存在: " + DEFAULT_TEMPLATE_CODE);
        }

        BookingConsolidatedDto dto = new BookingConsolidatedDto();

        // 提取业务数据 (Map形式，方便前端回显)
        Map<String, Object> businessData = mapJsonToMap(dataJson);

        // 转换为驼峰命名供前端使用
        Map<String, Object> camelCaseData = convertToCamelCase(businessData);
        dto.setBusinessData(camelCaseData);

        // 从模版配置中提取坐标信息
        Map<String, FieldLocation> fieldLocations = parseTemplateFieldConfig(template.getFieldConfig());
        dto.setFieldLocations(fieldLocations);
        log.info("解析到 {} 个字段坐标", fieldLocations.size());

        // 生成 UUID 并缓存到 Redis (30分钟有效期)
        String uuid = UUID.randomUUID().toString();
        dto.setUuid(uuid);

        // 缓存原始文件路径和模版路径，方便后续生成 PDF 时使用
        camelCaseData.put("originalFilePath", filePath);
        camelCaseData.put("templateFilePath", template.getTemplateFilePath());

        redisTemplate.opsForValue().set(REDIS_PREFIX + uuid, dto, 30, TimeUnit.MINUTES);
        log.info("分析完成，UUID: {}, 业务数据（驼峰命名）: {}", uuid, camelCaseData);

        return dto;
    }

    /**
     * 3. AI智能提取 - 生成最终PDF并保存：接收用户确认数据 -> 修改PDF -> 保存DB
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingConsolidated generateAndSavePdf(BookingConsolidatedDto userDto) {
        log.info("开始生成PDF，UUID: {}", userDto.getUuid());

        // 从 Redis 获取缓存 (主要为了获取模版路径和原始坐标)
        BookingConsolidatedDto cachedDto = (BookingConsolidatedDto) redisTemplate.opsForValue()
                .get(REDIS_PREFIX + userDto.getUuid());
        if (cachedDto == null) {
            throw new RuntimeException("会话已过期，请重新上传文件");
        }

        // 获取 PDF 模版文件路径
        Map<String, Object> cachedData = (Map<String, Object>) cachedDto.getBusinessData();
        String templateFilePath = (String) cachedData.get("templateFilePath");

        if (StringUtils.isEmpty(templateFilePath)) {
            throw new RuntimeException("PDF 模版路径不存在");
        }

        // 合并数据：从缓存获取完整数据，然后用用户编辑的数据覆盖
        Map<String, Object> mergedData = new HashMap<>(cachedData);
        Map<String, Object> userData = (Map<String, Object>) userDto.getBusinessData();
        if (userData != null) {
            mergedData.putAll(userData);
        }
        log.info("合并后的数据（原始）: {}", mergedData);

        // 应用业务规则（blNo/bookingNo同步、运费逻辑等）
        log.info("应用业务规则...");
        com.ruoyi.system.utils.BillOfLadingValidator.applyBusinessRules(mergedData);
        log.info("业务规则应用完成");

        // 转换字段名：驼峰 -> 下划线（用于数据库保存）
        Map<String, Object> dbData = convertCamelToUnderscore(mergedData);
        log.info("转换后的数据（数据库格式）: {}", dbData);

        // 确保 booking_no 存在，如果不存在则生成一个
        if (StringUtils.isEmpty((String) dbData.get("booking_no"))) {
            String generatedBookingNo = "BK" + System.currentTimeMillis();
            dbData.put("booking_no", generatedBookingNo);
            mergedData.put("bookingNo", generatedBookingNo); // 同步到mergedData
            log.warn("booking_no 不存在，生成默认值: {}", generatedBookingNo);
        }

        // 更新缓存中的业务数据为合并后的完整数据（保留驼峰命名用于PDF编辑）
        cachedDto.setBusinessData(mergedData);

        // 修改 PDF (使用模版文件，抹除旧数据，写入新数据)
        // 注意：cachedDto 中包含 fieldLocations (模版坐标)，mergedData 中包含完整的业务数据
        String newPdfPath;
        try {
            newPdfPath = PdfEditUtils.modifyPdf(templateFilePath, cachedDto);
            log.info("PDF生成成功: {}", newPdfPath);
        } catch (IOException e) {
            log.error("PDF生成失败", e);
            throw new RuntimeException("PDF生成失败: " + e.getMessage());
        }

        // 保存到数据库（使用转换后的下划线命名数据，保存到新的bill_of_lading表）
        com.ruoyi.system.domain.BillOfLading bl = mapMapToBillOfLading(dbData);
        bl.setFilePath(newPdfPath);

        // 确保bl_no存在
        if (StringUtils.isEmpty(bl.getBlNo())) {
            bl.setBlNo("BL" + System.currentTimeMillis());
        }

        log.info("准备插入bill_of_lading表，bl_no: {}, booking_no: {}, 文件路径: {}",
                bl.getBlNo(), bl.getBookingNo(), newPdfPath);
        billOfLadingMapper.insertBillOfLading(bl);

        // 为了兼容性，也返回BookingConsolidated对象（后续可以统一为BillOfLading）
        BookingConsolidated bc = new BookingConsolidated();
        bc.setBookingNo(bl.getBookingNo());
        bc.setFilePath(newPdfPath);
        return bc;
    }

    /**
     * 4. 仅生成PDF（不保存到数据库）- 调用Dify生成HTML并转换为PDF字节数组
     * 
     * @param userDto 用户编辑后的数据
     * @return PDF文件路径（临时文件，用于浏览器下载）
     */
    @Override
    public String generatePdfOnly(BookingConsolidatedDto userDto) {
        log.info("仅生成PDF（通过Dify HTML转换），UUID: {}", userDto.getUuid());

        // 从 Redis 获取缓存
        BookingConsolidatedDto cachedDto = (BookingConsolidatedDto) redisTemplate.opsForValue()
                .get(REDIS_PREFIX + userDto.getUuid());
        if (cachedDto == null) {
            throw new RuntimeException("会话已过期，请重新上传文件");
        }

        // 合并数据：从缓存获取完整数据，然后用用户编辑的数据覆盖
        Map<String, Object> cachedData = (Map<String, Object>) cachedDto.getBusinessData();
        Map<String, Object> mergedData = new HashMap<>(cachedData);
        Map<String, Object> userData = (Map<String, Object>) userDto.getBusinessData();
        if (userData != null) {
            mergedData.putAll(userData);
        }
        log.info("合并后的数据: {}", mergedData);

        // 应用业务规则
        com.ruoyi.system.utils.BillOfLadingValidator.applyBusinessRules(mergedData);

        // 调用Dify生成HTML并转换为PDF
        try {
            byte[] pdfBytes = generatePdfFromDifyHtml(mergedData, DIFY_API_KEY_GENERATE_HTML);

            // 保存为临时文件供下载
            String tempFileName = "BL_" + System.currentTimeMillis() + ".pdf";
            String tempFilePath = RuoYiConfig.getProfile() + "/temp/" + tempFileName;
            java.io.File tempFile = new java.io.File(tempFilePath);
            tempFile.getParentFile().mkdirs();

            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(tempFile)) {
                fos.write(pdfBytes);
            }

            log.info("PDF生成成功（仅导出）: {}, 大小: {} 字节", tempFilePath, pdfBytes.length);
            return tempFilePath;

        } catch (Exception e) {
            log.error("PDF生成失败", e);
            throw new RuntimeException("PDF生成失败: " + e.getMessage(), e);
        }
    }

    /**
     * 5. 只保存到数据库（不生成PDF）
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingConsolidated saveToDbOnly(BookingConsolidatedDto userDto) {
        log.info("只保存数据库，UUID: {}", userDto.getUuid());

        // 从 Redis 获取缓存
        BookingConsolidatedDto cachedDto = (BookingConsolidatedDto) redisTemplate.opsForValue()
                .get(REDIS_PREFIX + userDto.getUuid());
        if (cachedDto == null) {
            throw new RuntimeException("会话已过期，请重新上传文件");
        }

        // 合并数据：从缓存获取完整数据，然后用用户编辑的数据覆盖
        Map<String, Object> cachedData = (Map<String, Object>) cachedDto.getBusinessData();
        Map<String, Object> mergedData = new HashMap<>(cachedData);
        Map<String, Object> userData = (Map<String, Object>) userDto.getBusinessData();
        if (userData != null) {
            mergedData.putAll(userData);
        }
        log.info("合并后的数据（原始）: {}", mergedData);

        // 应用业务规则（blNo/bookingNo同步、运费逻辑等）
        log.info("应用业务规则...");
        com.ruoyi.system.utils.BillOfLadingValidator.applyBusinessRules(mergedData);
        log.info("业务规则应用完成");

        // 转换字段名：驼峰 -> 下划线（用于数据库保存）
        Map<String, Object> dbData = convertCamelToUnderscore(mergedData);
        log.info("转换后的数据（数据库格式）: {}", dbData);

        // 确保 booking_no 存在
        if (StringUtils.isEmpty((String) dbData.get("booking_no"))) {
            String generatedBookingNo = "BK" + System.currentTimeMillis();
            dbData.put("booking_no", generatedBookingNo);
            log.warn("booking_no 不存在，生成默认值: {}", generatedBookingNo);
        }

        // 保存到数据库
        com.ruoyi.system.domain.BillOfLading bl = mapMapToBillOfLading(dbData);

        // 确俛l_no存在
        if (StringUtils.isEmpty(bl.getBlNo())) {
            bl.setBlNo("BL" + System.currentTimeMillis());
        }

        log.info("准备插入bill_of_lading_v3表，bl_no: {}, booking_no: {}",
                bl.getBlNo(), bl.getBookingNo());
        billOfLadingMapper.insertBillOfLading(bl);

        // 返回结果
        BookingConsolidated bc = new BookingConsolidated();
        bc.setBookingNo(bl.getBookingNo());
        return bc;
    }

    // ================= 私有辅助方法 =================

    /**
     * 调用 Dify 工作流并解析结果
     */
    private JSONObject callDifyWorkflow(String filePath, String apiKey) {
        if (StringUtils.isEmpty(filePath))
            return null;

        // 处理本地路径
        String localPath = filePath;
        if (filePath.contains(Constants.RESOURCE_PREFIX)) {
            localPath = filePath.substring(filePath.indexOf(Constants.RESOURCE_PREFIX));
        }
        if (localPath.startsWith(Constants.RESOURCE_PREFIX)) {
            localPath = localPath.replaceFirst(Constants.RESOURCE_PREFIX, RuoYiConfig.getProfile());
        }
        try {
            localPath = java.net.URLDecoder.decode(localPath, Constants.UTF8);
        } catch (Exception e) {
            log.error("URL解码失败: {}", localPath, e);
        }

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
            body.add("user", "user-system");

            HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
            ResponseEntity<String> response = restTemplate.postForEntity(uploadUrl, requestEntity, String.class);

            if (!response.getStatusCode().is2xxSuccessful()) {
                log.error("Dify文件上传失败: {}", response.getBody());
                return null;
            }
            String uploadFileId = JSON.parseObject(response.getBody()).getString("id");

            // 2. 运行工作流
            String workflowUrl = DIFY_BASE_URL + "/workflows/run";
            HttpHeaders workflowHeaders = new HttpHeaders();
            workflowHeaders.setContentType(MediaType.APPLICATION_JSON);
            workflowHeaders.set("Authorization", "Bearer " + apiKey);

            JSONObject fileInput = new JSONObject();
            fileInput.put("type",
                    localPath.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$") ? "image" : "document");
            fileInput.put("transfer_method", "local_file");
            fileInput.put("upload_file_id", uploadFileId);

            JSONObject inputs = new JSONObject();
            inputs.put("file", fileInput);

            JSONObject workflowBody = new JSONObject();
            workflowBody.put("inputs", inputs);
            workflowBody.put("response_mode", "blocking");
            workflowBody.put("user", "user-system");

            HttpEntity<String> workflowRequest = new HttpEntity<>(workflowBody.toJSONString(), workflowHeaders);
            ResponseEntity<String> workflowResponse = restTemplate.postForEntity(workflowUrl, workflowRequest,
                    String.class);

            if (!workflowResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Dify工作流调用失败: {}", workflowResponse.getBody());
                return null;
            }

            // 3. 解析结果
            JSONObject workflowResult = JSON.parseObject(workflowResponse.getBody());
            JSONObject outputs = workflowResult.getJSONObject("data").getJSONObject("outputs");

            if (outputs.containsKey("text")) {
                String jsonText = outputs.getString("text");
                if (jsonText.contains("```json")) {
                    jsonText = jsonText.replaceAll("```json", "").replaceAll("```", "");
                }
                return JSON.parseObject(jsonText);
            }
        } catch (Exception e) {
            log.error("Dify 调用异常", e);
        }
        return null;
    }

    /**
     * 调用Dify工作流生成HTML并转换为PDF
     * 
     * @param data   业务数据（Map格式）
     * @param apiKey Dify API密钥
     * @return PDF字节数组
     */
    private byte[] generatePdfFromDifyHtml(Map<String, Object> data, String apiKey) {
        log.info("调用Dify工作流生成HTML，数据: {}", data);

        RestTemplate restTemplate = new RestTemplate();
        try {
            // 调用工作流
            String workflowUrl = DIFY_BASE_URL + "/workflows/run";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("Authorization", "Bearer " + apiKey);

            // 构建请求体 - 将业务数据作为JSON对象传递给"data"字段
            JSONObject inputs = new JSONObject();
            inputs.put("data", data); // Dify工作流期望一个名为"data"的dict类型input字段

            JSONObject workflowBody = new JSONObject();
            workflowBody.put("inputs", inputs);
            workflowBody.put("response_mode", "blocking");
            workflowBody.put("user", "user-system");

            HttpEntity<String> workflowRequest = new HttpEntity<>(workflowBody.toJSONString(), headers);
            ResponseEntity<String> workflowResponse = restTemplate.postForEntity(workflowUrl, workflowRequest,
                    String.class);

            if (!workflowResponse.getStatusCode().is2xxSuccessful()) {
                log.error("Dify工作流调用失败: {}", workflowResponse.getBody());
                throw new RuntimeException("Dify工作流调用失败");
            }

            // 解析响应获取HTML
            JSONObject workflowResult = JSON.parseObject(workflowResponse.getBody());
            JSONObject outputs = workflowResult.getJSONObject("data").getJSONObject("outputs");

            // 从output字段获取HTML
            String htmlContent = null;
            if (outputs.containsKey("output")) {
                htmlContent = outputs.getString("output");
            } else if (outputs.containsKey("text")) {
                htmlContent = outputs.getString("text");
            }

            if (StringUtils.isEmpty(htmlContent)) {
                throw new RuntimeException("Dify工作流未返回HTML内容");
            }

            log.info("Dify返回HTML长度: {} 字符", htmlContent.length());

            // 使用HtmlToPdfConverter转换HTML为PDF
            byte[] pdfBytes = com.ruoyi.system.utils.HtmlToPdfConverter.convertHtmlToPdfBytes(htmlContent);
            log.info("HTML转PDF成功，PDF大小: {} 字节", pdfBytes.length);

            return pdfBytes;

        } catch (Exception e) {
            log.error("生成PDF失败", e);
            throw new RuntimeException("生成PDF失败: " + e.getMessage(), e);
        }
    }

    /**
     * 将 JSON 转换为 Entity (保留原有逻辑)
     */
    private BookingConsolidated mapJsonToEntity(JSONObject dataJson) {
        BookingConsolidated bc = new BookingConsolidated();
        Map<String, Object> map = mapJsonToMap(dataJson);
        return mapMapToEntity(map);
    }

    /**
     * 将 Map 转换为 Entity
     */
    private BookingConsolidated mapMapToEntity(Map<String, Object> map) {
        BookingConsolidated bc = new BookingConsolidated();
        bc.setBookingNo((String) map.get("booking_no"));
        bc.setShipper((String) map.get("shipper"));
        bc.setConsignee((String) map.get("consignee"));
        bc.setNotifyParty((String) map.get("notify_party"));
        bc.setVesselVoyage((String) map.get("vessel_voyage"));
        bc.setPortOfLoading((String) map.get("port_of_loading"));
        bc.setPortOfDischarge((String) map.get("port_of_discharge"));
        bc.setPlaceOfDelivery((String) map.get("place_of_delivery"));
        bc.setCargoDescription((String) map.get("cargo_description"));
        bc.setCargoQuantity((String) map.get("cargo_quantity"));

        Object gw = map.get("cargo_gross_weight");
        if (gw instanceof BigDecimal)
            bc.setCargoGrossWeight((BigDecimal) gw);
        else if (gw instanceof String)
            bc.setCargoGrossWeight(extractBigDecimal((String) gw));

        Object meas = map.get("cargo_measurement");
        if (meas instanceof BigDecimal)
            bc.setCargoMeasurement((BigDecimal) meas);
        else if (meas instanceof String)
            bc.setCargoMeasurement(extractBigDecimal((String) meas));

        bc.setContainerNo((String) map.get("container_no"));
        bc.setSealNo((String) map.get("seal_no"));

        // 注意：marks, freight_term, vessel_name, voyage_no 等字段
        // 可能在数据库中不存在，如果存在则需要添加对应的setter
        // 这里假设只使用现有的数据库字段

        return bc;
    }

    /**
     * 将 Map (下划线命名) 转换为 BookingConsolidated Entity
     * 用于同时保存到 booking_consolidated 表
     */
    private BookingConsolidated mapMapToBookingConsolidated(Map<String, Object> map) {
        BookingConsolidated bc = new BookingConsolidated();

        bc.setBookingNo((String) map.get("booking_no"));
        bc.setShipper((String) map.get("shipper"));
        bc.setConsignee((String) map.get("consignee"));
        bc.setNotifyParty((String) map.get("notify_party"));
        bc.setVesselVoyage((String) map.get("vessel_voyage"));
        bc.setPortOfLoading((String) map.get("port_of_loading"));
        bc.setPortOfDischarge((String) map.get("port_of_discharge"));
        bc.setPlaceOfDelivery((String) map.get("place_of_delivery"));
        bc.setCargoDescription((String) map.get("goods_description"));

        // 处理 package_quantity → cargoQuantity
        Object pkgQty = map.get("package_quantity");
        Object pkgUnit = map.get("package_unit");
        if (pkgQty != null && pkgUnit != null) {
            bc.setCargoQuantity(pkgQty.toString() + " " + pkgUnit.toString());
        } else if (pkgQty != null) {
            bc.setCargoQuantity(pkgQty.toString());
        }

        // 处理 gross_weight_kgs
        Object gw = map.get("gross_weight_kgs");
        if (gw instanceof BigDecimal) {
            bc.setCargoGrossWeight((BigDecimal) gw);
        } else if (gw instanceof String) {
            bc.setCargoGrossWeight(extractBigDecimal((String) gw));
        } else if (gw instanceof Number) {
            bc.setCargoGrossWeight(new BigDecimal(gw.toString()));
        }

        // 处理 measurement_cbm
        Object meas = map.get("measurement_cbm");
        if (meas instanceof BigDecimal) {
            bc.setCargoMeasurement((BigDecimal) meas);
        } else if (meas instanceof String) {
            bc.setCargoMeasurement(extractBigDecimal((String) meas));
        } else if (meas instanceof Number) {
            bc.setCargoMeasurement(new BigDecimal(meas.toString()));
        }

        // 处理 container_no / seal_no
        String containerSeal = (String) map.get("container_seal_info");
        if (!StringUtils.isEmpty(containerSeal)) {
            String[] parts = containerSeal.split("/");
            if (parts.length >= 1) {
                bc.setContainerNo(parts[0].trim());
            }
            if (parts.length >= 2) {
                bc.setSealNo(parts[1].trim());
            }
        } else {
            bc.setContainerNo((String) map.get("container_no"));
            bc.setSealNo((String) map.get("seal_no"));
        }

        // VGM 字段
        Object vgm = map.get("vgm");
        if (vgm instanceof BigDecimal) {
            bc.setVgm((BigDecimal) vgm);
        } else if (vgm instanceof String) {
            bc.setVgm(extractBigDecimal((String) vgm));
        } else if (vgm instanceof Number) {
            bc.setVgm(new BigDecimal(vgm.toString()));
        }

        bc.setVgmUnit((String) map.get("vgm_unit"));

        return bc;
    }

    /**
     * 将 JSON 转换为 Map (支持扁平结构和驼峰命名)
     */
    private Map<String, Object> mapJsonToMap(JSONObject dataJson) {
        Map<String, Object> map = new HashMap<>();

        log.info("开始映射Dify JSON数据，字段数量: {}", dataJson.size());

        // 字段映射：Dify驼峰命名 -> 数据库下划线命名
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("blNo", "bl_no");
        fieldMapping.put("bookingNo", "booking_no");
        fieldMapping.put("docNo", "doc_no");
        fieldMapping.put("serialNo", "serial_no");
        fieldMapping.put("shipper", "shipper");
        fieldMapping.put("consignee", "consignee");
        fieldMapping.put("notifyParty", "notify_party");
        fieldMapping.put("carrierAgent", "carrier_agent");
        fieldMapping.put("deliveryAgent", "delivery_agent");
        fieldMapping.put("vesselName", "vessel_name");
        fieldMapping.put("voyageNo", "voyage_no");
        fieldMapping.put("placeOfReceipt", "place_of_receipt");
        fieldMapping.put("portOfLoading", "port_of_loading");
        fieldMapping.put("portOfDischarge", "port_of_discharge");
        fieldMapping.put("placeOfDelivery", "place_of_delivery");
        fieldMapping.put("containerNo", "container_no");
        fieldMapping.put("sealNo", "seal_no");
        fieldMapping.put("packageQuantity", "package_quantity");
        fieldMapping.put("description", "goods_description");
        fieldMapping.put("marks", "goods_description");
        fieldMapping.put("grossWeight", "gross_weight");
        fieldMapping.put("measurement", "measurement");
        fieldMapping.put("serviceType", "service_type");
        fieldMapping.put("revenueTons", "revenue_tons");
        fieldMapping.put("freightTerm", "freight_term");
        fieldMapping.put("freightRate", "freight_rate");
        fieldMapping.put("prepaidAmount", "prepaid_amount");
        fieldMapping.put("collectAmount", "collect_amount");
        fieldMapping.put("payableAt", "payable_at");
        fieldMapping.put("originalBlCount", "original_bl_count");
        fieldMapping.put("issuePlace", "issue_place");
        fieldMapping.put("ladenOnBoard", "laden_on_board");

        // 遍历映射表，从JSON提取数据
        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            String difyField = entry.getKey();
            String dbField = entry.getValue();

            if (dataJson.containsKey(difyField)) {
                Object value = dataJson.get(difyField);

                // 处理数值类型字段
                if (dbField.equals("cargo_gross_weight") || dbField.equals("cargo_measurement")) {
                    if (value instanceof String) {
                        map.put(dbField, extractBigDecimal((String) value));
                    } else if (value instanceof Number) {
                        map.put(dbField, new BigDecimal(value.toString()));
                    }
                } else {
                    map.put(dbField, value != null ? value.toString() : null);
                }

                log.debug("映射字段: {} -> {} = {}", difyField, dbField, value);
            } else {
                log.debug("Dify未返回字段: {}", difyField);
            }
        }

        // 特殊处理：合并vesselName和voyageNo（智能检测避免重复）
        if (map.containsKey("vessel_name") && map.containsKey("voyage_no")) {
            String vessel = String.valueOf(map.get("vessel_name"));
            String voyage = String.valueOf(map.get("voyage_no"));

            // 检查vesselName是否已包含voyageNo（避免重复拼接）
            if (!vessel.contains(voyage)) {
                map.put("vessel_voyage", (vessel + " " + voyage).trim());
                log.debug("合并字段: vessel_voyage = {} + {}", vessel, voyage);
            } else {
                map.put("vessel_voyage", vessel.trim());
                log.debug("vesselName已包含航次，直接使用: vessel_voyage = {}", vessel);
            }
        } else if (map.containsKey("vessel_name")) {
            map.put("vessel_voyage", map.get("vessel_name"));
        } else if (map.containsKey("voyage_no")) {
            map.put("vessel_voyage", map.get("voyage_no"));
        }

        log.info("映射完成，共提取 {} 个字段", map.size());
        return map;
    }

    /**
     * 解析模版字段配置 JSON
     * 格式: {"field_name": {"page": 1, "x": 100, "y": 200, "w": 150, "h": 20}}
     */
    private Map<String, FieldLocation> parseTemplateFieldConfig(String fieldConfigJson) {
        Map<String, FieldLocation> locations = new HashMap<>();

        if (StringUtils.isEmpty(fieldConfigJson)) {
            return locations;
        }

        try {
            JSONObject config = JSON.parseObject(fieldConfigJson);
            for (Map.Entry<String, Object> entry : config.entrySet()) {
                String fieldName = entry.getKey();
                JSONObject locObj = (JSONObject) entry.getValue();

                FieldLocation loc = new FieldLocation();
                loc.setPage(locObj.getIntValue("page"));
                loc.setX(locObj.getFloatValue("x"));
                loc.setY(locObj.getFloatValue("y"));
                loc.setW(locObj.getFloatValue("w"));
                loc.setH(locObj.getFloatValue("h"));

                locations.put(fieldName, loc);
            }
        } catch (Exception e) {
            log.error("解析模版字段配置失败", e);
        }

        return locations;
    }

    private BigDecimal extractBigDecimal(String val) {
        if (StringUtils.isEmpty(val))
            return null;
        try {
            // 提取数字部分 (移除 "KGS", "CBM" 等)
            String num = val.replaceAll("[^0-9.]", "");
            return new BigDecimal(num);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 将驼峰命名的Map转换为下划线命名的Map（用于数据库字段映射）
     */
    private Map<String, Object> convertCamelToUnderscore(Map<String, Object> camelMap) {
        Map<String, Object> underscoreMap = new HashMap<>();

        // 字段映射：驼峰 -> 下划线
        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("blNo", "bl_no");
        fieldMapping.put("bookingNo", "booking_no");
        fieldMapping.put("docNo", "doc_no");
        fieldMapping.put("serialNo", "serial_no");
        fieldMapping.put("shipper", "shipper");
        fieldMapping.put("consignee", "consignee");
        fieldMapping.put("notifyParty", "notify_party");
        fieldMapping.put("carrierAgent", "carrier_agent");
        fieldMapping.put("deliveryAgent", "delivery_agent");
        fieldMapping.put("vesselVoyage", "vessel_voyage");
        fieldMapping.put("placeOfReceipt", "place_of_receipt");
        fieldMapping.put("portOfLoading", "port_of_loading");
        fieldMapping.put("portOfDischarge", "port_of_discharge");
        fieldMapping.put("placeOfDelivery", "place_of_delivery");
        fieldMapping.put("containerSealInfo", "container_seal_info");
        fieldMapping.put("packageUnit", "package_unit");
        fieldMapping.put("goodsDescription", "goods_description");
        fieldMapping.put("description", "goods_description");
        fieldMapping.put("marks", "goods_description");
        fieldMapping.put("grossWeightKgs", "gross_weight_kgs");
        fieldMapping.put("measurementCbm", "measurement_cbm");
        fieldMapping.put("serviceType", "service_type");
        fieldMapping.put("revenueTons", "revenue_tons");
        fieldMapping.put("freightTerm", "freight_term");
        fieldMapping.put("freightRate", "freight_rate");
        fieldMapping.put("prepaidAmount", "prepaid_amount");
        fieldMapping.put("collectAmount", "collect_amount");
        fieldMapping.put("payableAt", "payable_at");
        fieldMapping.put("originalBlCount", "original_bl_count");
        fieldMapping.put("issuePlace", "issue_place");
        fieldMapping.put("ladenOnBoard", "laden_on_board");

        // 特殊处理：vesselName + voyageNo -> vessel_voyage
        if (camelMap.containsKey("vesselName") || camelMap.containsKey("voyageNo")) {
            String vessel = getStringHelper(camelMap, "vesselName");
            String voyage = getStringHelper(camelMap, "voyageNo");
            underscoreMap.put("vessel_voyage", (vessel + " " + voyage).trim());
        }

        // 特殊处理：containerNo + sealNo -> container_seal_info
        if (camelMap.containsKey("containerNo") || camelMap.containsKey("sealNo")) {
            String container = getStringHelper(camelMap, "containerNo");
            String seal = getStringHelper(camelMap, "sealNo");
            underscoreMap.put("container_seal_info", (container + " / " + seal).trim());
        }

        // 特殊处理：packageQuantity 拆分 "748 CARTONS"
        if (camelMap.containsKey("packageQuantity")) {
            String pkgQty = getStringHelper(camelMap, "packageQuantity");
            if (!StringUtils.isEmpty(pkgQty)) {
                String[] parts = pkgQty.trim().split("\\s+", 2);
                try {
                    underscoreMap.put("package_quantity", Integer.parseInt(parts[0]));
                    if (parts.length > 1)
                        underscoreMap.put("package_unit", parts[1]);
                } catch (NumberFormatException e) {
                    log.warn("无法解析packageQuantity: {}", pkgQty);
                }
            }
        }

        // 特殊处理：grossWeight 提取数字 "20030 KGS"
        if (camelMap.containsKey("grossWeight")) {
            String weight = getStringHelper(camelMap, "grossWeight");
            if (!StringUtils.isEmpty(weight)) {
                try {
                    String numStr = weight.replaceAll("[^0-9.]", "").trim();
                    if (!StringUtils.isEmpty(numStr)) {
                        underscoreMap.put("gross_weight_kgs", new BigDecimal(numStr));
                    }
                } catch (Exception e) {
                    log.warn("无法解析grossWeight: {}", weight);
                }
            }
        }

        // 特殊处理：measurement 提取数字 "68 CBM"
        if (camelMap.containsKey("measurement")) {
            String measure = getStringHelper(camelMap, "measurement");
            if (!StringUtils.isEmpty(measure)) {
                try {
                    String numStr = measure.replaceAll("[^0-9.]", "").trim();
                    if (!StringUtils.isEmpty(numStr)) {
                        underscoreMap.put("measurement_cbm", new BigDecimal(numStr));
                    }
                } catch (Exception e) {
                    log.warn("无法解析measurement: {}", measure);
                }
            }
        }

        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            String camelKey = entry.getKey();
            String underscoreKey = entry.getValue();
            if (camelMap.containsKey(camelKey)) {
                Object value = camelMap.get(camelKey);
                if (value != null && !StringUtils.isEmpty(value.toString())) {
                    if (!underscoreMap.containsKey(underscoreKey)) {
                        underscoreMap.put(underscoreKey, value);
                    }
                }
            }
        }

        // 保留内部字段
        if (camelMap.containsKey("originalFilePath")) {
            underscoreMap.put("originalFilePath", camelMap.get("originalFilePath"));
        }
        if (camelMap.containsKey("templateFilePath")) {
            underscoreMap.put("templateFilePath", camelMap.get("templateFilePath"));
        }

        log.info("字段转换：输入{}个 -> 输出{}个", camelMap.size(), underscoreMap.size());
        return underscoreMap;
    }

    // 辅助方法：安全获取字符串
    private String getStringHelper(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value != null ? value.toString() : "";
    }

    /**
     * 将 Map 转换为 BillOfLading Entity
     */
    private com.ruoyi.system.domain.BillOfLading mapMapToBillOfLading(Map<String, Object> map) {
        com.ruoyi.system.domain.BillOfLading bl = new com.ruoyi.system.domain.BillOfLading();

        bl.setBlNo((String) map.get("bl_no"));
        bl.setBookingNo((String) map.get("booking_no"));
        bl.setDocNo((String) map.get("doc_no"));
        bl.setShipper((String) map.get("shipper"));
        bl.setConsignee((String) map.get("consignee"));
        bl.setNotifyParty((String) map.get("notify_party"));
        bl.setCarrierAgent((String) map.get("carrier_agent"));
        bl.setDeliveryAgent((String) map.get("delivery_agent"));
        bl.setVesselVoyage((String) map.get("vessel_voyage"));
        bl.setPlaceOfReceipt((String) map.get("place_of_receipt"));
        bl.setPortOfLoading((String) map.get("port_of_loading"));
        bl.setPortOfDischarge((String) map.get("port_of_discharge"));
        bl.setPlaceOfDelivery((String) map.get("place_of_delivery"));
        bl.setContainerSealInfo((String) map.get("container_seal_info"));

        // 包装数量
        Object pkgQty = map.get("package_quantity");
        if (pkgQty instanceof Integer) {
            bl.setPackageQuantity((Integer) pkgQty);
        } else if (pkgQty instanceof String) {
            try {
                bl.setPackageQuantity(Integer.parseInt((String) pkgQty));
            } catch (Exception e) {
                log.warn("无法解析package_quantity: {}", pkgQty);
            }
        }

        bl.setPackageUnit((String) map.get("package_unit"));
        bl.setGoodsDescription((String) map.get("goods_description"));

        // 毛重
        Object gw = map.get("gross_weight_kgs");
        if (gw instanceof BigDecimal) {
            bl.setGrossWeightKgs((BigDecimal) gw);
        } else if (gw instanceof String) {
            bl.setGrossWeightKgs(extractBigDecimal((String) gw));
        }

        // 体积
        Object meas = map.get("measurement_cbm");
        if (meas instanceof BigDecimal) {
            bl.setMeasurementCbm((BigDecimal) meas);
        } else if (meas instanceof String) {
            bl.setMeasurementCbm(extractBigDecimal((String) meas));
        }

        bl.setFreightTerm((String) map.get("freight_term"));
        bl.setOriginalBlCount((String) map.get("original_bl_count"));
        bl.setIssuePlace((String) map.get("issue_place"));

        return bl;
    }

    /**
     * 将下划线命名转换为驼峰命名（用于前端显示）
     */
    private Map<String, Object> convertToCamelCase(Map<String, Object> snakeMap) {
        Map<String, Object> camelMap = new HashMap<>();

        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("bl_no", "blNo");
        fieldMapping.put("booking_no", "bookingNo");
        fieldMapping.put("doc_no", "docNo");
        fieldMapping.put("serial_no", "serialNo");
        fieldMapping.put("shipper", "shipper");
        fieldMapping.put("consignee", "consignee");
        fieldMapping.put("notify_party", "notifyParty");
        fieldMapping.put("carrier_agent", "carrierAgent");
        fieldMapping.put("delivery_agent", "deliveryAgent");
        fieldMapping.put("vessel_voyage", "vesselVoyage");
        fieldMapping.put("vessel_name", "vesselName");
        fieldMapping.put("voyage_no", "voyageNo");
        fieldMapping.put("place_of_receipt", "placeOfReceipt");
        fieldMapping.put("port_of_loading", "portOfLoading");
        fieldMapping.put("port_of_discharge", "portOfDischarge");
        fieldMapping.put("place_of_delivery", "placeOfDelivery");
        fieldMapping.put("container_seal_info", "containerSealInfo");
        fieldMapping.put("container_no", "containerNo");
        fieldMapping.put("seal_no", "sealNo");
        fieldMapping.put("package_quantity", "packageQuantity");
        fieldMapping.put("package_unit", "packageUnit");
        fieldMapping.put("goods_description", "goodsDescription");
        fieldMapping.put("gross_weight_kgs", "grossWeightKgs");
        fieldMapping.put("gross_weight", "grossWeight");
        fieldMapping.put("measurement_cbm", "measurementCbm");
        fieldMapping.put("measurement", "measurement");
        fieldMapping.put("service_type", "serviceType");
        fieldMapping.put("revenue_tons", "revenueTons");
        fieldMapping.put("freight_term", "freightTerm");
        fieldMapping.put("freight_rate", "freightRate");
        fieldMapping.put("prepaid_amount", "prepaidAmount");
        fieldMapping.put("collect_amount", "collectAmount");
        fieldMapping.put("payable_at", "payableAt");
        fieldMapping.put("original_bl_count", "originalBlCount");
        fieldMapping.put("issue_place", "issuePlace");
        fieldMapping.put("laden_on_board", "ladenOnBoard");
        fieldMapping.put("originalFilePath", "originalFilePath");
        fieldMapping.put("templateFilePath", "templateFilePath");

        for (Map.Entry<String, Object> entry : snakeMap.entrySet()) {
            String snakeKey = entry.getKey();
            String camelKey = fieldMapping.getOrDefault(snakeKey, snakeKey);
            camelMap.put(camelKey, entry.getValue());
        }

        log.debug("字段名转换：{} 个字段从snake_case转为camelCase", camelMap.size());
        return camelMap;
    }
}

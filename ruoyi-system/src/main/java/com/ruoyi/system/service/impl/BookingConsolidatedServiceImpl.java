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
    private static final String DIFY_BASE_URL = "http://localhost/v1";
    private static final String REDIS_PREFIX = "pdf_edit:";
    private static final String DEFAULT_TEMPLATE_CODE = "booking_standard";

    @Autowired
    private BookingConsolidatedMapper bookingConsolidatedMapper;

    @Autowired
    private com.ruoyi.system.mapper.BillOfLadingMapper billOfLadingMapper;

    @Autowired
    private BillOfLadingExportService exportService;

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

        log.info("准备插入bill_of_lading_v5表，bl_no: {}, booking_no: {}, 文件路径: {}",
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

        Map<String, Object> mergedData = new HashMap<>();

        // 1. 如果有 UUID，尝试从 Redis 获取缓存
        if (StringUtils.isNotEmpty(userDto.getUuid())) {
            BookingConsolidatedDto cachedDto = (BookingConsolidatedDto) redisTemplate.opsForValue()
                    .get(REDIS_PREFIX + userDto.getUuid());
            if (cachedDto != null) {
                Map<String, Object> cachedData = (Map<String, Object>) cachedDto.getBusinessData();
                if (cachedData != null) {
                    mergedData.putAll(cachedData);
                }
            }
        }

        // 2. 使用用户提交的数据覆盖（支持无 UUID 导出）
        Map<String, Object> userData = (Map<String, Object>) userDto.getBusinessData();
        if (userData != null) {
            mergedData.putAll(userData);
        }

        if (mergedData.isEmpty()) {
            throw new RuntimeException("导出失败：数据为空");
        }

        log.info("合并后的导出数据: {}", mergedData);

        // 应用业务规则
        com.ruoyi.system.utils.BillOfLadingValidator.applyBusinessRules(mergedData);

        // 添加全面的字段别名，确保 Dify 模板能识别
        addComprehensiveAliases(mergedData);

        log.info("添加别名后的导出数据字段: {}", mergedData.keySet());

        // 调用 Word 模板填充与 PDF 转换服务
        try {
            byte[] pdfBytes = exportService.exportToPdf(mergedData);

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

        log.info("准备插入bill_of_lading_v5表，bl_no: {}, booking_no: {}",
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

            if (outputs.containsKey("text") || outputs.containsKey("output")) {
                String rawText = outputs.containsKey("text") ? outputs.getString("text") : outputs.getString("output");
                if (StringUtils.isEmpty(rawText))
                    return null;

                // 鲁棒的 JSON 提取逻辑：寻找第一个 { 和最后一个 }
                try {
                    String jsonPart = rawText;
                    if (jsonPart.contains("```")) {
                        jsonPart = jsonPart.replaceAll("(?s)```[a-z]*", "").replaceAll("```", "");
                    }

                    int start = jsonPart.indexOf("{");
                    int end = jsonPart.lastIndexOf("}");
                    if (start >= 0 && end > start) {
                        jsonPart = jsonPart.substring(start, end + 1);
                    }
                    return JSON.parseObject(jsonPart.trim());
                } catch (Exception e) {
                    log.warn("分析 JSON 失败，回退到原始解析: {}", e.getMessage());
                    return JSON.parseObject(rawText);
                }
            }
        } catch (Exception e) {
            log.error("Dify 调用异常", e);
        }
        return null;
    }

    /**
     * 为 Map 添加别名（如果原字段存在且目标字段不存在）
     */
    private void addAlias(Map<String, Object> map, String key, String... aliases) {
        if (!map.containsKey(key)) {
            // 如果主键缺失，尝试从别名中找一个填补
            for (String alias : aliases) {
                if (map.containsKey(alias)) {
                    map.put(key, map.get(alias));
                    break;
                }
            }
        }

        // 确保所有别名也都有值
        if (map.containsKey(key)) {
            Object value = map.get(key);
            for (String alias : aliases) {
                if (!map.containsKey(alias)) {
                    map.put(alias, value);
                }
            }
        }
    }

    /**
     * 添加全面的字段别名，确保 Dify 模板能识别各种命名格式
     */
    private void addComprehensiveAliases(Map<String, Object> map) {
        // 港口和地点字段
        addAlias(map, "portOfLoading", "port_of_loading", "PORT_OF_LOADING", "PortOfLoading", "pol", "POL");
        addAlias(map, "portOfDischarge", "port_of_discharge", "PORT_OF_DISCHARGE", "PortOfDischarge", "pod", "POD");
        addAlias(map, "placeOfDelivery", "place_of_delivery", "PLACE_OF_DELIVERY", "PlaceOfDelivery");
        addAlias(map, "placeOfReceipt", "place_of_receipt", "PLACE_OF_RECEIPT", "PlaceOfReceipt");

        // 船名航次
        addAlias(map, "vesselVoyage", "vessel_voyage", "VESSEL_VOYAGE", "VesselVoyage", "oceanVessel", "ocean_vessel",
                "OCEAN_VESSEL");
        addAlias(map, "vesselName", "vessel_name", "VESSEL_NAME", "VesselName", "vessel");
        addAlias(map, "voyageNo", "voyage_no", "VOYAGE_NO", "VoyageNo", "voyage");
        addAlias(map, "preCarriageBy", "pre_carriage_by", "PRE_CARRIAGE_BY", "PreCarriageBy", "pre_carriage",
                "PRE_CARRIAGE");

        // 集装箱和封条
        addAlias(map, "containerNo", "container_no", "CONTAINER_NO", "ContainerNo", "container", "CONTAINER");
        addAlias(map, "sealNo", "seal_no", "SEAL_NO", "SealNo", "seal", "SEAL");
        addAlias(map, "containerSealInfo", "container_seal_info", "CONTAINER_SEAL_INFO", "ContainerSealInfo",
                "containerSeal", "container_seal");

        // 包装和货物
        addAlias(map, "packageQuantity", "package_quantity", "PACKAGE_QUANTITY", "PackageQuantity", "noOfPkgs",
                "NO_OF_PKGS", "packages", "PACKAGES");
        addAlias(map, "packageUnit", "package_unit", "PACKAGE_UNIT", "PackageUnit");
        addAlias(map, "goodsDescription", "goods_description", "GOODS_DESCRIPTION", "GoodsDescription", "description",
                "DESCRIPTION", "cargoDescription", "cargo_description");
        addAlias(map, "marks", "MARKS", "Marks", "shippingMarks", "shipping_marks");

        // 重量和体积
        addAlias(map, "grossWeight", "gross_weight", "GROSS_WEIGHT", "GrossWeight", "weight", "WEIGHT");
        addAlias(map, "grossWeightKgs", "gross_weight_kgs", "GROSS_WEIGHT_KGS", "GrossWeightKgs");
        addAlias(map, "measurement", "MEASUREMENT", "Measurement", "volume", "VOLUME");
        addAlias(map, "measurementCbm", "measurement_cbm", "MEASUREMENT_CBM", "MeasurementCbm");
        addAlias(map, "containerWeight", "container_weight", "CONTAINER_WEIGHT", "ContainerWeight", "boxWeight",
                "box_weight");
        addAlias(map, "vgmWeight", "vgm_weight", "VGM_WEIGHT", "VgmWeight", "vgm", "VGM");

        // 运费相关
        addAlias(map, "freightTerm", "freight_term", "FREIGHT_TERM", "FreightTerm", "freightCharges", "freight_charges",
                "FREIGHT_CHARGES");
        addAlias(map, "freightRate", "freight_rate", "FREIGHT_RATE", "FreightRate", "rate", "RATE", "Rate");
        addAlias(map, "prepaidAmount", "prepaid_amount", "PREPAID_AMOUNT", "PrepaidAmount", "prepaid", "PREPAID",
                "Prepaid");
        addAlias(map, "collectAmount", "collect_amount", "COLLECT_AMOUNT", "CollectAmount", "collect", "COLLECT",
                "Collect");
        addAlias(map, "payableAt", "payable_at", "PAYABLE_AT", "PayableAt");
        addAlias(map, "revenueTons", "revenue_tons", "REVENUE_TONS", "RevenueTons", "revTons", "REV_TONS");

        // 提单信息
        addAlias(map, "blNo", "bl_no", "BL_NO", "BlNo", "billOfLadingNo", "bill_of_lading_no");
        addAlias(map, "bookingNo", "booking_no", "BOOKING_NO", "BookingNo");
        addAlias(map, "docNo", "doc_no", "DOC_NO", "DocNo", "documentNo", "document_no");
        addAlias(map, "serialNo", "serial_no", "SERIAL_NO", "SerialNo");
        addAlias(map, "originalBlCount", "original_bl_count", "ORIGINAL_BL_COUNT", "OriginalBlCount", "originalBl",
                "original_bl");

        // 当事人信息
        addAlias(map, "shipper", "SHIPPER", "Shipper");
        addAlias(map, "consignee", "CONSIGNEE", "Consignee");
        addAlias(map, "notifyParty", "notify_party", "NOTIFY_PARTY", "NotifyParty");
        addAlias(map, "carrierAgent", "carrier_agent", "CARRIER_AGENT", "CarrierAgent");
        addAlias(map, "deliveryAgent", "delivery_agent", "DELIVERY_AGENT", "DeliveryAgent");

        // 其他字段
        addAlias(map, "serviceType", "service_type", "SERVICE_TYPE", "ServiceType", "service", "SERVICE");
        addAlias(map, "serviceMode", "service_mode", "SERVICE_MODE", "ServiceMode", "mode", "MODE");
        addAlias(map, "issuePlace", "issue_place", "ISSUE_PLACE", "IssuePlace");
        addAlias(map, "ladenOnBoard", "laden_on_board", "LADEN_ON_BOARD", "LadenOnBoard", "ladenOnBoardDate",
                "laden_on_board_date");

        log.debug("别名添加完成，当前字段数量: {}", map.size());
    }

    private Map<String, Object> mapJsonToMap(JSONObject dataJson) {
        Map<String, Object> map = new HashMap<>();

        log.info("开始映射Dify JSON数据，字段数量: {}", dataJson.size());

        // 核心字段矩阵：定义数据库字段名及其对应的候选 Dify 字段名（含驼峰和下划线）
        Map<String, String[]> mappingMatrix = new HashMap<>();
        mappingMatrix.put("bl_no", new String[] { "blNo", "bl_no", "bl_number", "bill_of_lading_no" });
        mappingMatrix.put("booking_no", new String[] { "bookingNo", "booking_no", "booking_number" });
        mappingMatrix.put("doc_no", new String[] { "docNo", "doc_no", "document_no" });
        mappingMatrix.put("serial_no", new String[] { "serialNo", "serial_no", "sequence_no" });
        mappingMatrix.put("shipper", new String[] { "shipper", "SHIPPER", "shipper_details" });
        mappingMatrix.put("consignee", new String[] { "consignee", "CONSIGNEE", "consignee_details" });
        mappingMatrix.put("notify_party", new String[] { "notifyParty", "notify_party", "notify" });
        mappingMatrix.put("carrier_agent", new String[] { "carrierAgent", "carrier_agent", "carrier" });
        mappingMatrix.put("delivery_agent", new String[] { "deliveryAgent", "delivery_agent", "destination_agent" });
        mappingMatrix.put("vessel_name", new String[] { "vesselName", "vessel_name", "vessel" });
        mappingMatrix.put("voyage_no", new String[] { "voyageNo", "voyage_no", "voyage" });
        mappingMatrix.put("vessel_voyage", new String[] { "vesselVoyage", "vessel_voyage", "ship_voyage" });
        mappingMatrix.put("place_of_receipt", new String[] { "placeOfReceipt", "place_of_receipt" });
        mappingMatrix.put("port_of_loading", new String[] { "portOfLoading", "port_of_loading", "pol" });
        mappingMatrix.put("port_of_discharge", new String[] { "portOfDischarge", "port_of_discharge", "pod" });
        mappingMatrix.put("place_of_delivery", new String[] { "placeOfDelivery", "place_of_delivery" });
        mappingMatrix.put("pre_carriage_by", new String[] { "preCarriageBy", "pre_carriage_by" });
        mappingMatrix.put("container_no", new String[] { "containerNo", "container_no", "cntr_no" });
        mappingMatrix.put("seal_no", new String[] { "sealNo", "seal_no", "seal" });
        mappingMatrix.put("container_weight", new String[] { "containerWeight", "container_weight", "tare_weight" });
        mappingMatrix.put("vgm_weight", new String[] { "vgmWeight", "vgm_weight", "vgm" });
        mappingMatrix.put("container_seal_info", new String[] { "containerSealInfo", "container_seal_info" });
        mappingMatrix.put("package_quantity", new String[] { "packageQuantity", "package_quantity" });
        mappingMatrix.put("package_unit", new String[] { "packageUnit", "package_unit", "unit" });
        mappingMatrix.put("goods_description",
                new String[] { "goodsDescription", "goods_description", "description", "cargo_description" });
        mappingMatrix.put("marks", new String[] { "marks", "MARKS", "shipping_marks" });
        mappingMatrix.put("gross_weight_kgs",
                new String[] { "grossWeightKgs", "gross_weight_kgs", "gross_weight", "grossWeight", "total_weight" });
        mappingMatrix.put("measurement_cbm",
                new String[] { "measurementCbm", "measurement_cbm", "measurement", "MEASUREMENT", "total_volume" });
        mappingMatrix.put("service_type", new String[] { "serviceType", "service_type", "type" });
        mappingMatrix.put("service_mode", new String[] { "serviceMode", "service_mode", "mode" });
        mappingMatrix.put("revenue_tons", new String[] { "revenueTons", "revenue_tons" });
        mappingMatrix.put("freight_term", new String[] { "freightTerm", "freight_term" });
        mappingMatrix.put("freight_rate", new String[] { "freightRate", "freight_rate", "rate" });
        mappingMatrix.put("prepaid_amount", new String[] { "prepaidAmount", "prepaid_amount" });
        mappingMatrix.put("collect_amount", new String[] { "collectAmount", "collect_amount" });
        mappingMatrix.put("payable_at", new String[] { "payableAt", "payable_at" });
        mappingMatrix.put("original_bl_count", new String[] { "originalBlCount", "original_bl_count" });
        mappingMatrix.put("issue_place", new String[] { "issuePlace", "issue_place" });
        mappingMatrix.put("laden_on_board", new String[] { "ladenOnBoard", "laden_on_board" });

        // 遍历矩阵进行深度优先匹配
        for (Map.Entry<String, String[]> entry : mappingMatrix.entrySet()) {
            String dbField = entry.getKey();
            String[] candidates = entry.getValue();

            for (String candidate : candidates) {
                if (dataJson.containsKey(candidate)) {
                    Object value = dataJson.get(candidate);
                    if (value != null) {
                        // 特殊处理数值类字段
                        String[] numericFields = { "gross_weight_kgs", "measurement_cbm", "container_weight",
                                "vgm_weight" };
                        boolean isNumeric = false;
                        for (String nf : numericFields)
                            if (nf.equals(dbField))
                                isNumeric = true;

                        if (isNumeric) {
                            map.put(dbField, extractBigDecimal(value));
                        } else if (dbField.equals("package_quantity")) {
                            // 件数可能是数字也可能是字符串 "100"
                            String valStr = value.toString().replaceAll("[^0-9]", "");
                            if (StringUtils.isNotEmpty(valStr)) {
                                map.put(dbField, Integer.parseInt(valStr));
                            }
                        } else {
                            map.put(dbField, value.toString());
                        }
                        log.debug("识别到字段: {} (来自 {}) = {}", dbField, candidate, value);
                        break; // 找到第一个匹配候选者即停止
                    }
                }
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
        fieldMapping.put("marks", "marks");
        fieldMapping.put("grossWeightKgs", "gross_weight_kgs");
        fieldMapping.put("total_weight", "gross_weight_kgs");
        fieldMapping.put("measurementCbm", "measurement_cbm");
        fieldMapping.put("total_volume", "measurement_cbm");
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
        fieldMapping.put("preCarriageBy", "pre_carriage_by");
        fieldMapping.put("serviceMode", "service_mode");
        fieldMapping.put("containerNo", "container_no");
        fieldMapping.put("sealNo", "seal_no");
        fieldMapping.put("containerWeight", "container_weight");
        fieldMapping.put("vgmWeight", "vgm_weight");
        fieldMapping.put("vgm", "vgm_weight");
        fieldMapping.put("filePath", "file_path");

        // 执行常规映射（来自前端确认表单的数据应拥有最高优先级）
        for (Map.Entry<String, String> entry : fieldMapping.entrySet()) {
            String camelKey = entry.getKey();
            String underscoreKey = entry.getValue();
            if (camelMap.containsKey(camelKey)) {
                Object value = camelMap.get(camelKey);
                if (value != null && !StringUtils.isEmpty(value.toString())) {
                    underscoreMap.put(underscoreKey, value);
                }
            }
        }

        // 特殊处理：辅助拆分与提取（仅在目标字段尚未由表单提供有效值时生效）
        // 1. vesselName + voyageNo -> vessel_voyage
        if (!underscoreMap.containsKey("vessel_voyage")
                || StringUtils.isEmpty(underscoreMap.get("vessel_voyage").toString())) {
            if (camelMap.containsKey("vesselName") || camelMap.containsKey("voyageNo")) {
                String vessel = getStringHelper(camelMap, "vesselName");
                String voyage = getStringHelper(camelMap, "voyageNo");
                underscoreMap.put("vessel_voyage", (vessel + " " + voyage).trim());
            }
        }

        // 2. containerNo + sealNo -> container_seal_info
        if (!underscoreMap.containsKey("container_seal_info")
                || StringUtils.isEmpty(underscoreMap.get("container_seal_info").toString())) {
            if (camelMap.containsKey("containerNo") || camelMap.containsKey("sealNo")) {
                String container = getStringHelper(camelMap, "containerNo");
                String seal = getStringHelper(camelMap, "sealNo");
                underscoreMap.put("container_seal_info", (container + " / " + seal).trim());
            }
        }

        // 3. packageQuantity 拆分 "748 CARTONS" (仅在 package_quantity 为空或为0时尝试)
        if (underscoreMap.get("package_quantity") == null
                || "0".equals(underscoreMap.get("package_quantity").toString())) {
            String pkgQty = getStringHelper(camelMap, "packageQuantity");
            if (!StringUtils.isEmpty(pkgQty) && pkgQty.contains(" ")) {
                String[] parts = pkgQty.trim().split("\\s+", 2);
                try {
                    underscoreMap.put("package_quantity", Integer.parseInt(parts[0]));
                    if (parts.length > 1 && !underscoreMap.containsKey("package_unit"))
                        underscoreMap.put("package_unit", parts[1]);
                } catch (NumberFormatException e) {
                }
            }
        }

        // 4. 数值提取 (grossWeight -> gross_weight_kgs, measurement -> measurement_cbm)
        extractNumericIfEmpty(camelMap, underscoreMap, "grossWeight", "gross_weight_kgs");
        extractNumericIfEmpty(camelMap, underscoreMap, "measurement", "measurement_cbm");

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

        bl.setBlNo((String) getVal(map, "bl_no", "blNo"));
        bl.setBookingNo((String) getVal(map, "booking_no", "bookingNo"));
        bl.setDocNo((String) getVal(map, "doc_no", "docNo"));
        bl.setSerialNo((String) getVal(map, "serial_no", "serialNo"));
        bl.setShipper((String) getVal(map, "shipper"));
        bl.setConsignee((String) getVal(map, "consignee"));
        bl.setNotifyParty((String) getVal(map, "notify_party", "notifyParty"));
        bl.setCarrierAgent((String) getVal(map, "carrier_agent", "carrierAgent"));
        bl.setDeliveryAgent((String) getVal(map, "delivery_agent", "deliveryAgent"));
        bl.setVesselVoyage((String) getVal(map, "vessel_voyage", "vesselVoyage"));
        bl.setPlaceOfReceipt((String) getVal(map, "place_of_receipt", "placeOfReceipt"));
        bl.setPortOfLoading((String) getVal(map, "port_of_loading", "portOfLoading"));
        bl.setPortOfDischarge((String) getVal(map, "port_of_discharge", "portOfDischarge"));
        bl.setPlaceOfDelivery((String) getVal(map, "place_of_delivery", "placeOfDelivery"));
        bl.setContainerSealInfo((String) getVal(map, "container_seal_info", "containerSealInfo"));

        // 包装数量
        Object pkgQty = getVal(map, "package_quantity", "packageQuantity", "cargo_quantity");
        if (pkgQty instanceof Integer) {
            bl.setPackageQuantity((Integer) pkgQty);
        } else if (pkgQty != null) {
            try {
                String s = pkgQty.toString().replaceAll("[^0-9]", "");
                if (!s.isEmpty())
                    bl.setPackageQuantity(Integer.parseInt(s));
            } catch (Exception e) {
                log.warn("无法解析package_quantity: {}", pkgQty);
            }
        }

        bl.setPackageUnit((String) getVal(map, "package_unit", "packageUnit"));
        bl.setGoodsDescription((String) getVal(map, "goods_description", "goodsDescription", "cargo_description"));

        // 毛重
        bl.setGrossWeightKgs(extractBigDecimal(
                getVal(map, "gross_weight_kgs", "grossWeightKgs", "cargo_gross_weight", "grossWeight")));

        // 体积
        bl.setMeasurementCbm(extractBigDecimal(
                getVal(map, "measurement_cbm", "measurementCbm", "cargo_measurement", "measurement")));

        bl.setFreightTerm((String) getVal(map, "freight_term", "freightTerm"));
        bl.setOriginalBlCount((String) getVal(map, "original_bl_count", "originalBlCount"));
        bl.setIssuePlace((String) getVal(map, "issue_place", "issuePlace"));
        bl.setServiceType((String) getVal(map, "service_type", "serviceType"));
        bl.setLadenOnBoard((String) getVal(map, "laden_on_board", "ladenOnBoard"));
        bl.setPayableAt((String) getVal(map, "payable_at", "payableAt"));
        bl.setPrepaidAmount((String) getVal(map, "prepaid_amount", "prepaidAmount"));
        bl.setCollectAmount((String) getVal(map, "collect_amount", "collectAmount"));
        bl.setFreightRate((String) getVal(map, "freight_rate", "freightRate", "rate"));
        bl.setRevenueTons((String) getVal(map, "revenue_tons", "revenueTons"));
        bl.setFilePath((String) getVal(map, "file_path", "filePath"));

        // V5 字段
        bl.setPreCarriageBy((String) getVal(map, "pre_carriage_by", "preCarriageBy"));
        bl.setServiceMode((String) getVal(map, "service_mode", "serviceMode"));
        bl.setContainerNo((String) getVal(map, "container_no", "containerNo"));
        bl.setSealNo((String) getVal(map, "seal_no", "sealNo"));
        bl.setContainerWeight(extractBigDecimal(getVal(map, "container_weight", "containerWeight")));
        bl.setVgmWeight(extractBigDecimal(getVal(map, "vgm_weight", "vgmWeight")));
        bl.setMarks((String) getVal(map, "marks", "MARKS"));

        return bl;
    }

    // 辅助方法：从Object中提取BigDecimal
    private BigDecimal extractBigDecimal(Object val) {
        if (val == null)
            return null;
        if (val instanceof BigDecimal)
            return (BigDecimal) val;
        if (val instanceof String) {
            String s = (String) val;
            if (StringUtils.isEmpty(s))
                return null;
            try {
                String num = s.replaceAll("[^0-9.]", "");
                if (StringUtils.isEmpty(num))
                    return null;
                return new BigDecimal(num);
            } catch (Exception e) {
                log.warn("无法从字符串 '{}' 解析BigDecimal: {}", s, e.getMessage());
                return null;
            }
        }
        log.warn("无法从类型 {} 解析BigDecimal: {}", val.getClass().getName(), val);
        return null;
    }

    // 辅助方法：安全获取Map中的值，支持多个key优先级
    private Object getVal(Map<String, Object> map, String... keys) {
        for (String key : keys) {
            if (map.containsKey(key)) {
                Object value = map.get(key);
                if (value != null && !StringUtils.isEmpty(value.toString())) {
                    return value;
                }
            }
        }
        return null;
    }

    private void extractNumericIfEmpty(Map<String, Object> camelMap, Map<String, Object> underscoreMap, String camelKey,
            String underscoreKey) {
        if (!underscoreMap.containsKey(underscoreKey) || underscoreMap.get(underscoreKey) == null) {
            String value = getStringHelper(camelMap, camelKey);
            if (!StringUtils.isEmpty(value)) {
                try {
                    String numStr = value.replaceAll("[^0-9.]", "").trim();
                    if (!StringUtils.isEmpty(numStr)) {
                        underscoreMap.put(underscoreKey, new BigDecimal(numStr));
                    }
                } catch (Exception e) {
                    log.warn("无法解析 {} 数值: {}", camelKey, value);
                }
            }
        }
    }

    /**
     * 将下划线命名转换为驼峰命名（用于前端显示）
     */
    private Map<String, Object> convertToCamelCase(Map<String, Object> snakeMap) {
        Map<String, Object> camelMap = new HashMap<>();

        Map<String, String> fieldMapping = new HashMap<>();
        fieldMapping.put("bl_no", "blNo");
        fieldMapping.put("bl_number", "blNo");
        fieldMapping.put("bill_of_lading_no", "blNo");
        fieldMapping.put("booking_no", "bookingNo");
        fieldMapping.put("booking_number", "bookingNo");
        fieldMapping.put("doc_no", "docNo");
        fieldMapping.put("document_no", "docNo");
        fieldMapping.put("serial_no", "serialNo");
        fieldMapping.put("sequence_no", "serialNo");
        fieldMapping.put("shipper", "shipper");
        fieldMapping.put("consignee", "consignee");
        fieldMapping.put("notify_party", "notifyParty");
        fieldMapping.put("carrier_agent", "carrierAgent");
        fieldMapping.put("delivery_agent", "deliveryAgent");
        fieldMapping.put("destination_agent", "deliveryAgent");
        fieldMapping.put("vessel_voyage", "vesselVoyage");
        fieldMapping.put("vessel_name", "vesselName");
        fieldMapping.put("voyage_no", "voyageNo");
        fieldMapping.put("place_of_receipt", "placeOfReceipt");
        fieldMapping.put("port_of_loading", "portOfLoading");
        fieldMapping.put("pol", "portOfLoading");
        fieldMapping.put("port_of_discharge", "portOfDischarge");
        fieldMapping.put("pod", "portOfDischarge");
        fieldMapping.put("place_of_delivery", "placeOfDelivery");
        fieldMapping.put("container_seal_info", "containerSealInfo");
        fieldMapping.put("container_no", "containerNo");
        fieldMapping.put("cntr_no", "containerNo");
        fieldMapping.put("seal_no", "sealNo");
        fieldMapping.put("package_quantity", "packageQuantity");
        fieldMapping.put("package_unit", "packageUnit");
        fieldMapping.put("unit", "packageUnit");
        fieldMapping.put("goods_description", "goodsDescription");
        fieldMapping.put("cargo_description", "goodsDescription");
        fieldMapping.put("gross_weight_kgs", "grossWeightKgs");
        fieldMapping.put("total_weight", "grossWeightKgs");
        fieldMapping.put("measurement_cbm", "measurementCbm");
        fieldMapping.put("total_volume", "measurementCbm");
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
        fieldMapping.put("pre_carriage_by", "preCarriageBy");
        fieldMapping.put("service_mode", "serviceMode");
        fieldMapping.put("container_weight", "containerWeight");
        fieldMapping.put("vgm_weight", "vgmWeight");
        fieldMapping.put("vgm", "vgmWeight");
        fieldMapping.put("marks", "marks");
        fieldMapping.put("shipping_marks", "marks");
        fieldMapping.put("file_path", "filePath");
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

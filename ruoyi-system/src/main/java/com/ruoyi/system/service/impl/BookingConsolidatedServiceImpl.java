package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.core.text.Convert;
import com.ruoyi.common.utils.DateUtils;
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
public class BookingConsolidatedServiceImpl implements IBookingConsolidatedService
{
    private static final Logger log = LoggerFactory.getLogger(BookingConsolidatedServiceImpl.class);

    private static final String DIFY_API_KEY_DIRECT = "app-TWO0gviA2zkp06u86rmEc2Ns";
    private static final String DIFY_API_KEY_ANALYZE = "app-qFk49MpWcQKiqY41Q7IdDwIj";
    private static final String DIFY_BASE_URL = "http://localhost/v1";
    private static final String REDIS_PREFIX = "pdf_edit:";

    @Autowired
    private BookingConsolidatedMapper bookingConsolidatedMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

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
    public int deleteBookingConsolidatedByBookingNos(String bookingNos) {
        return bookingConsolidatedMapper.deleteBookingConsolidatedByBookingNos(Convert.toStrArray(bookingNos));
    }

    @Override
    public int deleteBookingConsolidatedByBookingNo(String bookingNo) {
        return bookingConsolidatedMapper.deleteBookingConsolidatedByBookingNo(bookingNo);
    }

    /**
     * 1. 直接保存模式：上传 -> 识别 -> 保存 (无人工介入)
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingConsolidated directProcessAndSave(String filePath) {
        // 调用 Dify 获取 JSON 数据 (使用直接保存的 API Key)
        JSONObject dataJson = callDifyWorkflow(filePath, DIFY_API_KEY_DIRECT);
        if (dataJson == null) {
            throw new RuntimeException("Dify 识别失败，未能获取有效数据");
        }

        // 转换为实体并保存
        BookingConsolidated bc = mapJsonToEntity(dataJson);
        bc.setFilePath(filePath);
        insertBookingConsolidated(bc);
        return bc;
    }

    /**
     * 2. AI智能提取 - 分析文件：上传 -> 识别 -> 缓存 -> 返回DTO (供前端确认)
     */
    @Override
    public BookingConsolidatedDto analyzeFile(String filePath) {
        // 调用 Dify 获取 JSON 数据 (使用分析模式的 API Key)
        JSONObject dataJson = callDifyWorkflow(filePath, DIFY_API_KEY_ANALYZE);
        if (dataJson == null) {
            throw new RuntimeException("Dify 识别失败");
        }

        BookingConsolidatedDto dto = new BookingConsolidatedDto();
        
        // 提取业务数据 (Map形式，方便前端回显)
        dto.setBusinessData(mapJsonToMap(dataJson));
        
        // 提取坐标信息 (若 Dify 返回了坐标)
        dto.setFieldLocations(extractLocations(dataJson));

        // 生成 UUID 并缓存到 Redis (30分钟有效期)
        String uuid = UUID.randomUUID().toString();
        dto.setUuid(uuid);
        
        // 缓存原始文件路径，方便后续生成 PDF 时使用 (放入 businessData 或单独缓存均可，这里暂存 Redis 方便)
        // 注意：DTO 序列化需要 RedisTemplate 配置正确
        // 这里我们将 filePath 也放入 businessData 或者 DTO 的某个字段，为了简单，我们假定前端会传回 filePath 或者我们在 Redis 里存一下
        // 修改 DTO 结构不太好，我们把 filePath 放入 businessData 的隐藏字段
        ((Map<String, Object>) dto.getBusinessData()).put("originalFilePath", filePath);

        redisTemplate.opsForValue().set(REDIS_PREFIX + uuid, dto, 30, TimeUnit.MINUTES);
        
        return dto;
    }

    /**
     * 3. AI智能提取 - 生成最终PDF并保存：接收用户确认数据 -> 修改PDF -> 保存DB
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingConsolidated generateAndSavePdf(BookingConsolidatedDto userDto) {
        // 从 Redis 获取缓存 (主要为了获取原始文件路径和原始坐标)
        BookingConsolidatedDto cachedDto = (BookingConsolidatedDto) redisTemplate.opsForValue().get(REDIS_PREFIX + userDto.getUuid());
        if (cachedDto == null) {
            throw new RuntimeException("会话已过期，请重新上传文件");
        }

        // 获取原始文件路径
        Map<String, Object> cachedData = (Map<String, Object>) cachedDto.getBusinessData();
        String originalFilePath = (String) cachedData.get("originalFilePath");

        // 更新缓存中的业务数据为用户提交的数据
        cachedDto.setBusinessData(userDto.getBusinessData());
        
        // 修改 PDF (抹除旧数据，写入新数据)
        // 注意：cachedDto 中包含 fieldLocations (原始坐标)，userDto 中包含 businessData (新值)
        String newPdfPath = originalFilePath;
        try {
            newPdfPath = PdfEditUtils.modifyPdf(originalFilePath, cachedDto);
        } catch (IOException e) {
            log.error("PDF生成失败", e);
            throw new RuntimeException("PDF生成失败: " + e.getMessage());
        }

        // 保存到数据库
        BookingConsolidated bc = mapMapToEntity((Map<String, Object>) userDto.getBusinessData());
        bc.setFilePath(newPdfPath);
        insertBookingConsolidated(bc);

        return bc;
    }

    // ================= 私有辅助方法 =================

    /**
     * 调用 Dify 工作流并解析结果
     */
    private JSONObject callDifyWorkflow(String filePath, String apiKey) {
        if (StringUtils.isEmpty(filePath)) return null;

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
            fileInput.put("type", localPath.toLowerCase().matches(".*\\.(jpg|jpeg|png|gif|bmp|webp)$") ? "image" : "document");
            fileInput.put("transfer_method", "local_file");
            fileInput.put("upload_file_id", uploadFileId);

            JSONObject inputs = new JSONObject();
            inputs.put("file", fileInput);

            JSONObject workflowBody = new JSONObject();
            workflowBody.put("inputs", inputs);
            workflowBody.put("response_mode", "blocking");
            workflowBody.put("user", "user-system");

            HttpEntity<String> workflowRequest = new HttpEntity<>(workflowBody.toJSONString(), workflowHeaders);
            ResponseEntity<String> workflowResponse = restTemplate.postForEntity(workflowUrl, workflowRequest, String.class);

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
        if (gw instanceof BigDecimal) bc.setCargoGrossWeight((BigDecimal) gw);
        else if (gw instanceof String) bc.setCargoGrossWeight(extractBigDecimal((String) gw));
        
        Object meas = map.get("cargo_measurement");
        if (meas instanceof BigDecimal) bc.setCargoMeasurement((BigDecimal) meas);
        else if (meas instanceof String) bc.setCargoMeasurement(extractBigDecimal((String) meas));

        bc.setContainerNo((String) map.get("container_no"));
        bc.setSealNo((String) map.get("seal_no"));
        return bc;
    }

    /**
     * 将 JSON 转换为 Map (扁平化处理)
     */
    private Map<String, Object> mapJsonToMap(JSONObject dataJson) {
        Map<String, Object> map = new HashMap<>();
        
        map.put("booking_no", dataJson.getString("booking_no"));
        map.put("shipper", dataJson.getString("shipper"));
        map.put("consignee", dataJson.getString("consignee"));
        map.put("notify_party", dataJson.getString("notify_party"));
        map.put("vessel_voyage", dataJson.getString("vessel_voyage"));
        map.put("port_of_loading", dataJson.getString("port_of_loading"));
        map.put("port_of_discharge", dataJson.getString("port_of_discharge"));
        map.put("place_of_delivery", dataJson.getString("place_of_delivery"));

        if (dataJson.containsKey("cargo_summary")) {
            JSONObject cargo = dataJson.getJSONObject("cargo_summary");
            map.put("cargo_description", cargo.getString("cargo_description"));
            map.put("cargo_quantity", cargo.getString("cargo_quantity"));
            map.put("cargo_gross_weight", extractBigDecimal(cargo.getString("cargo_gross_weight")));
            map.put("cargo_measurement", extractBigDecimal(cargo.getString("cargo_measurement")));
        }

        if (dataJson.containsKey("containers")) {
            JSONArray containers = dataJson.getJSONArray("containers");
            if (containers != null && !containers.isEmpty()) {
                JSONObject firstContainer = containers.getJSONObject(0);
                map.put("container_no", firstContainer.getString("container_no"));
                map.put("seal_no", firstContainer.getString("seal_no"));
            }
        }
        return map;
    }

    /**
     * 提取坐标信息 (模拟或从 JSON 提取)
     */
    private Map<String, FieldLocation> extractLocations(JSONObject dataJson) {
        Map<String, FieldLocation> locations = new HashMap<>();
        // 这里的逻辑取决于 Dify 是否返回坐标。
        // 假设 Dify 返回的每个字段如果是个对象 {value: "...", rect: [x,y,w,h]} 则可以提取
        // 目前如果 Dify 只返回纯文本 JSON，我们无法准确获取坐标。
        // 为了演示，我们检查是否有 suffix "_rect" 或 "_bbox" 的字段，或者字段本身是对象
        
        // 遍历 mapJsonToMap 的 key，尝试寻找对应的坐标
        // 暂时假设没有坐标返回，避免空指针。如果未来 Dify 升级支持坐标，这里需要解析。
        // 用户目前的 Prompt 可能不支持坐标。
        // 但为了 PdfEditUtils 不报错，我们不需要放入无效坐标。PdfEditUtils 会跳过 null location。
        
        return locations;
    }

    private BigDecimal extractBigDecimal(String val) {
        if (StringUtils.isEmpty(val)) return null;
        try {
            // 提取数字部分 (移除 "KGS", "CBM" 等)
            String num = val.replaceAll("[^0-9.]", "");
            return new BigDecimal(num);
        } catch (Exception e) {
            return null;
        }
    }
}

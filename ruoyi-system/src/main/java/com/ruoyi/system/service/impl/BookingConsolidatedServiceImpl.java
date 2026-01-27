package com.ruoyi.system.service.impl;

import java.math.BigDecimal;
import java.util.List;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.common.constant.Constants;
import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.ruoyi.system.mapper.BookingConsolidatedMapper;
import com.ruoyi.system.domain.BookingConsolidated;
import com.ruoyi.system.service.IBookingConsolidatedService;
import com.ruoyi.common.core.text.Convert;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

/**
 * 订舱与集装箱合并信息Service业务层处理
 * * @author ruoyi
 * @date 2026-01-27
 */
@Service
public class BookingConsolidatedServiceImpl implements IBookingConsolidatedService
{
    private static final Logger log = LoggerFactory.getLogger(BookingConsolidatedServiceImpl.class);

    private static final String DIFY_API_KEY = "app-TWO0gviA2zkp06u86rmEc2Ns";
    private static final String DIFY_BASE_URL = "http://localhost/v1";

    @Autowired
    private BookingConsolidatedMapper bookingConsolidatedMapper;

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

    @Override
    @Transactional(rollbackFor = Exception.class)
    public BookingConsolidated getMessageFromFlow(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return null;
        }

        // 1. 本地路径处理
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
            // 2. 上传文件到 Dify
            String uploadUrl = DIFY_BASE_URL + "/files/upload";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.MULTIPART_FORM_DATA);
            headers.set("Authorization", "Bearer " + DIFY_API_KEY);

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

            // 3. 调用 Dify 工作流
            String workflowUrl = DIFY_BASE_URL + "/workflows/run";
            HttpHeaders workflowHeaders = new HttpHeaders();
            workflowHeaders.setContentType(MediaType.APPLICATION_JSON);
            workflowHeaders.set("Authorization", "Bearer " + DIFY_API_KEY);

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
                return null;
            }

            // 4. 解析 Dify 返回的 JSON (根据用户提供的实际结构)
            JSONObject workflowResult = JSON.parseObject(workflowResponse.getBody());
            JSONObject outputs = workflowResult.getJSONObject("data").getJSONObject("outputs");

            BookingConsolidated bc = new BookingConsolidated();

            if (outputs.containsKey("text")) {
                String jsonText = outputs.getString("text");
                if (jsonText.contains("```json")) {
                    jsonText = jsonText.replaceAll("```json", "").replaceAll("```", "");
                }

                JSONObject dataJson = JSON.parseObject(jsonText);

                // A. 基础字段映射 (匹配下划线命名)
                bc.setBookingNo(dataJson.getString("booking_no"));
                bc.setShipper(dataJson.getString("shipper"));
                bc.setConsignee(dataJson.getString("consignee"));
                bc.setNotifyParty(dataJson.getString("notify_party"));
                bc.setVesselVoyage(dataJson.getString("vessel_voyage"));
                bc.setPortOfLoading(dataJson.getString("port_of_loading"));
                bc.setPortOfDischarge(dataJson.getString("port_of_discharge"));
                bc.setPlaceOfDelivery(dataJson.getString("place_of_delivery"));

                // B. 货物信息 (从 cargo_summary 嵌套提取)
                if (dataJson.containsKey("cargo_summary")) {
                    JSONObject cargo = dataJson.getJSONObject("cargo_summary");
                    bc.setCargoDescription(cargo.getString("cargo_description"));
                    bc.setCargoQuantity(cargo.getString("cargo_quantity"));
                    // 清洗数字字符串中的单位，如 "21180 KGS" -> "21180"
                    bc.setCargoGrossWeight(extractBigDecimal(cargo.getString("cargo_gross_weight")));
                    bc.setCargoMeasurement(extractBigDecimal(cargo.getString("cargo_measurement")));
                }

                // C. 集装箱信息 (从 containers 数组提取第1个)
                if (dataJson.containsKey("containers")) {
                    JSONArray containers = dataJson.getJSONArray("containers");
                    if (containers != null && !containers.isEmpty()) {
                        JSONObject firstContainer = containers.getJSONObject(0);
                        bc.setContainerNo(firstContainer.getString("container_no"));
                        bc.setSealNo(firstContainer.getString("seal_no"));
                        bc.setVgm(extractBigDecimal(firstContainer.getString("vgm")));
                        bc.setVgmUnit(firstContainer.getString("vgm_unit"));
                    }
                }

                bc.setFilePath(filePath);

                // 5. 保存并返回
                if (StringUtils.isNotEmpty(bc.getBookingNo())) {
                    this.insertBookingConsolidated(bc);
                    return bc;
                }
            }
        } catch (Exception e) {
            log.error("处理工作流数据异常", e);
        }
        return null;
    }

    /**
     * 工具方法：从带单位的字符串中提取数字并转为 BigDecimal
     * 例如 "21180 KGS" -> 21180
     */
    private BigDecimal extractBigDecimal(String value) {
        if (StringUtils.isEmpty(value)) return null;
        try {
            String cleaned = value.replaceAll("[^0-9.]", "");
            return new BigDecimal(cleaned);
        } catch (Exception e) {
            return null;
        }
    }
}
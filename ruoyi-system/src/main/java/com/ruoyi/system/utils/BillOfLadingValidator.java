package com.ruoyi.system.utils;

import com.ruoyi.common.utils.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * 提单业务规则验证器
 * 用于在PDF填充和数据保存前应用业务规则
 * 
 * @author ruoyi
 * @date 2026-01-30
 */
public class BillOfLadingValidator {
    private static final Logger log = LoggerFactory.getLogger(BillOfLadingValidator.class);

    /**
     * 应用所有业务规则到数据Map
     * 
     * @param data 待处理的业务数据（驼峰命名）
     * @return 应用规则后的数据
     */
    public static Map<String, Object> applyBusinessRules(Map<String, Object> data) {
        if (data == null || data.isEmpty()) {
            log.warn("业务数据为空，跳过规则验证");
            return data;
        }

        log.info("开始应用业务规则，数据字段数: {}", data.size());

        // 规则1: 强制同步 blNo 和 bookingNo
        syncBlNoAndBookingNo(data);

        // 规则2: 处理运费逻辑分支
        processFreightTermLogic(data);

        // 规则3: 自动补全重量单位
        autoAppendWeightUnit(data);

        log.info("业务规则应用完成");
        return data;
    }

    /**
     * 规则1: 强制同步 blNo 和 bookingNo
     * 如果两者不一致，以 blNo 为准覆盖 bookingNo
     */
    private static void syncBlNoAndBookingNo(Map<String, Object> data) {
        String blNo = getStringValue(data, "blNo");
        String bookingNo = getStringValue(data, "bookingNo");

        if (StringUtils.isEmpty(blNo)) {
            log.debug("blNo 为空，跳过同步规则");
            return;
        }

        if (!blNo.equals(bookingNo)) {
            log.warn("检测到 blNo ({}) 与 bookingNo ({}) 不一致，强制使用 blNo 覆盖 bookingNo",
                    blNo, bookingNo);
            data.put("bookingNo", blNo);
        } else {
            log.debug("blNo 和 bookingNo 一致: {}", blNo);
        }
    }

    /**
     * 规则2: 处理运费逻辑分支
     * - Freight Collect -> COLLECT = "AS ARRANGED", PREPAID = null
     * - Freight Prepaid -> PREPAID = 数值或"AS ARRANGED", COLLECT = null
     */
    private static void processFreightTermLogic(Map<String, Object> data) {
        String freightTerm = getStringValue(data, "freightTerm");

        if (StringUtils.isEmpty(freightTerm)) {
            log.debug("freightTerm 为空，跳过运费逻辑处理");
            return;
        }

        log.info("处理运费逻辑，freightTerm: {}", freightTerm);

        if (freightTerm.equalsIgnoreCase("Freight Collect") ||
                freightTerm.contains("Collect")) {
            // 到付模式
            String collectAmount = getStringValue(data, "collectAmount");
            if (StringUtils.isEmpty(collectAmount)) {
                data.put("collectAmount", "AS ARRANGED");
                log.info("运费模式为到付，设置 collectAmount = AS ARRANGED");
            }
            // 清空预付金额
            data.put("prepaidAmount", null);
            log.info("运费模式为到付，清空 prepaidAmount");

        } else if (freightTerm.equalsIgnoreCase("Freight Prepaid") ||
                freightTerm.contains("Prepaid")) {
            // 预付模式
            String prepaidAmount = getStringValue(data, "prepaidAmount");
            if (StringUtils.isEmpty(prepaidAmount)) {
                data.put("prepaidAmount", "AS ARRANGED");
                log.info("运费模式为预付，设置 prepaidAmount = AS ARRANGED");
            }
            // 清空到付金额
            data.put("collectAmount", null);
            log.info("运费模式为预付，清空 collectAmount");
        } else {
            log.debug("未识别的运费条款: {}，不进行特殊处理", freightTerm);
        }
    }

    /**
     * 规则3: 自动补全重量单位
     * 如果 grossWeight 不包含 "KGS"，自动追加
     */
    private static void autoAppendWeightUnit(Map<String, Object> data) {
        // 处理多种可能的重量字段名
        String[] weightFields = { "grossWeight", "grossWeightKgs", "cargoGrossWeight" };

        for (String fieldName : weightFields) {
            String weight = getStringValue(data, fieldName);
            if (!StringUtils.isEmpty(weight)) {
                // 如果是纯数字，追加 KGS
                if (weight.matches("^\\d+(\\.\\d+)?$")) {
                    String newValue = weight + " KGS";
                    data.put(fieldName, newValue);
                    log.info("自动补全重量单位: {} -> {}", weight, newValue);
                } else if (!weight.toUpperCase().contains("KGS")) {
                    // 如果不包含 KGS，追加
                    String newValue = weight + " KGS";
                    data.put(fieldName, newValue);
                    log.info("自动补全重量单位: {} -> {}", weight, newValue);
                }
            }
        }
    }

    /**
     * 辅助方法：安全获取字符串值
     */
    private static String getStringValue(Map<String, Object> data, String key) {
        Object value = data.get(key);
        if (value == null) {
            return null;
        }
        return value.toString().trim();
    }

    /**
     * 验证必填字段
     * 
     * @param data           业务数据
     * @param requiredFields 必填字段列表
     * @throws IllegalArgumentException 如果必填字段缺失
     */
    public static void validateRequiredFields(Map<String, Object> data, String... requiredFields) {
        for (String field : requiredFields) {
            String value = getStringValue(data, field);
            if (StringUtils.isEmpty(value)) {
                throw new IllegalArgumentException("必填字段缺失: " + field);
            }
        }
    }

    /**
     * 清理和标准化数据
     * 移除空值、trim字符串等
     */
    public static void cleanAndNormalizeData(Map<String, Object> data) {
        data.entrySet().removeIf(entry -> entry.getValue() == null);

        data.forEach((key, value) -> {
            if (value instanceof String) {
                String trimmed = ((String) value).trim();
                data.put(key, trimmed.isEmpty() ? null : trimmed);
            }
        });
    }
}

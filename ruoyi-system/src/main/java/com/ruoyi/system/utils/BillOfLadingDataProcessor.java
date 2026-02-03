package com.ruoyi.system.utils;

import com.ruoyi.common.utils.StringUtils;
import java.util.HashMap;
import java.util.Map;

/**
 * 提单数据预处理工具类
 * 负责清洗 Dify 返回的原始数据，为 Word 模板填充做准备
 */
public class BillOfLadingDataProcessor {

    /**
     * 清洗提单数据
     * 
     * @param rawData Dify 返回的原始数据 Map
     * @return 清洗后的数据 Map
     */
    public static Map<String, Object> process(Map<String, Object> rawData) {
        Map<String, Object> processed = new HashMap<>();

        // 预设 V5 核心字段为空字符串，确保未提取到的字段在 PDF 中显示为空白
        String[] v5Fields = {
                "blNo", "bookingNo", "docNo", "serialNo", "shipper", "consignee", "notifyParty",
                "carrierAgent", "deliveryAgent", "preCarriageBy", "vesselVoyage", "placeOfReceipt",
                "portOfLoading", "portOfDischarge", "placeOfDelivery", "containerNo", "sealNo",
                "containerWeight", "vgmWeight", "containerSealInfo", "packageQuantity", "packageUnit",
                "goodsDescription", "grossWeightKgs", "measurementCbm", "serviceType", "serviceMode",
                "revenueTons", "freightTerm", "freightRate", "prepaidAmount", "collectAmount",
                "payableAt", "originalBlCount", "issuePlace", "ladenOnBoard", "marks"
        };
        for (String field : v5Fields) {
            processed.put(field, "");
            processed.put(field + "1", "");
            processed.put(toSnakeCase(field) + "1", "");
        }
        // 特殊占位符初始化
        processed.put("PRE-CARRIAGE BY", "");
        processed.put("Carrier Agent1", "");
        processed.put("service-mode1", "");
        processed.put("collect_type1", "");
        processed.put("container_number1", "");
        processed.put("seal_number1", "");
        processed.put("container_weight1", "");
        processed.put("VGM", "");

        if (rawData == null) {
            return processed;
        }

        for (Map.Entry<String, Object> entry : rawData.entrySet()) {
            String originalKey = entry.getKey();
            Object value = entry.getValue();

            // 1. 处理 "N/A" 或 null
            String valStr = "";
            if (value != null && !"N/A".equalsIgnoreCase(value.toString())
                    && !"None".equalsIgnoreCase(value.toString())) {
                valStr = value.toString().trim();
            }

            // 2. 清洗数据内容
            Object finalValue = valStr;
            if (isNumericField(originalKey)) {
                finalValue = cleanNumeric(valStr);
            }

            // 3. 映射到 Word 模板标签
            String snakeKey = toSnakeCase(originalKey);
            processed.put(originalKey, finalValue);
            processed.put(originalKey + "1", finalValue);
            processed.put(snakeKey + "1", finalValue);

            // 4. V4/V5 特殊自定义映射逻辑
            if ("preCarriageBy".equalsIgnoreCase(originalKey) || "pre_carriage_by".equalsIgnoreCase(originalKey)) {
                processed.put("PRE-CARRIAGE BY", finalValue);
            }
            if ("carrierAgent".equalsIgnoreCase(originalKey) || "carrier_agent".equalsIgnoreCase(originalKey)) {
                processed.put("Carrier Agent1", finalValue);
            }
            if ("serviceMode".equalsIgnoreCase(originalKey) || "service_mode".equalsIgnoreCase(originalKey)) {
                processed.put("service-mode1", finalValue);
            }
            if ("collectAmount".equalsIgnoreCase(originalKey) || "collect_amount".equalsIgnoreCase(originalKey)) {
                processed.put("collect_type1", finalValue);
            }

            // V5 新增映射
            if ("containerNo".equalsIgnoreCase(originalKey) || "container_no".equalsIgnoreCase(originalKey)) {
                processed.put("container_number1", finalValue);
            }
            if ("sealNo".equalsIgnoreCase(originalKey) || "seal_no".equalsIgnoreCase(originalKey)) {
                processed.put("seal_number1", finalValue);
            }
            if ("containerWeight".equalsIgnoreCase(originalKey) || "container_weight".equalsIgnoreCase(originalKey)) {
                processed.put("container_weight1", finalValue);
            }
            if ("vgmWeight".equalsIgnoreCase(originalKey) || "vgm_weight".equalsIgnoreCase(originalKey)) {
                processed.put("VGM", finalValue);
            }
        }

        // 5. 增强逻辑：根据运费条款自动设置 "AS ARRANGED"
        String freightTerm = (String) processed.getOrDefault("freightTerm", "");
        if (StringUtils.isNotEmpty(freightTerm)) {
            String termUpper = freightTerm.toUpperCase();
            if (termUpper.contains("PREPAID")) {
                processed.put("prepaidAmount", "AS ARRANGED");
                processed.put("prepaid_amount1", "AS ARRANGED");
                processed.put("prepaidAmount1", "AS ARRANGED");
            }
            if (termUpper.contains("COLLECT")) {
                processed.put("collectAmount", "AS ARRANGED");
                processed.put("collect_amount1", "AS ARRANGED");
                processed.put("collectAmount1", "AS ARRANGED");
                processed.put("collect_type1", "AS ARRANGED");
            }
        }

        return processed;
    }

    /**
     * 驼峰转下划线 (优化版)
     */
    private static String toSnakeCase(String str) {
        if (str == null)
            return null;
        if (str.contains("_"))
            return str.toLowerCase();

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (Character.isUpperCase(c)) {
                if (i > 0)
                    sb.append("_");
                sb.append(Character.toLowerCase(c));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    /**
     * 判断是否为数值字段
     */
    private static boolean isNumericField(String key) {
        if (key == null)
            return false;
        String lowerKey = key.toLowerCase();
        return lowerKey.contains("weight") || lowerKey.contains("measurement")
                || lowerKey.contains("amount") || lowerKey.contains("quantity") || lowerKey.contains("rate");
    }

    /**
     * 清洗数值字符串，去掉单位，保留纯数字
     */
    private static String cleanNumeric(String val) {
        if (StringUtils.isEmpty(val)) {
            return "";
        }
        String cleaned = val.replaceAll("[^0-9.]", "");
        if (StringUtils.isEmpty(cleaned)) {
            return "";
        }
        if (cleaned.indexOf(".") != cleaned.lastIndexOf(".")) {
            int firstDot = cleaned.indexOf(".");
            cleaned = cleaned.substring(0, firstDot + 1) + cleaned.substring(firstDot + 1).replace(".", "");
        }
        return cleaned;
    }
}

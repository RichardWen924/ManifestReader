package com.manifest.service.utils;

import cn.hutool.core.util.StrUtil;
import java.util.HashMap;
import java.util.Map;

/**
 * 提单数据预处理工具
 * 负责清洗 Dify 返回的原始数据：N/A处理、驼峰转换、Word模版标签映射
 */
public class BillOfLadingDataProcessor {

    private static final String[] CORE_FIELDS = {
            "blNo", "bookingNo", "docNo", "serialNo", "shipper", "consignee", "notifyParty",
            "carrierAgent", "deliveryAgent", "preCarriageBy", "vesselVoyage", "placeOfReceipt",
            "portOfLoading", "portOfDischarge", "placeOfDelivery", "containerNo", "sealNo",
            "containerWeight", "vgmWeight", "packageQuantity", "packageUnit", "goodsDescription",
            "grossWeightKgs", "measurementCbm", "serviceType", "serviceMode", "revenueTons",
            "freightTerm", "freightRate", "prepaidAmount", "collectAmount",
            "payableAt", "originalBlCount", "issuePlace", "ladenOnBoard", "marks"
    };

    public static Map<String, Object> process(Map<String, Object> rawData) {
        Map<String, Object> processed = new HashMap<>();

        // 预初始化所有标准字段为空字符串，防止模版渲染报错
        for (String field : CORE_FIELDS) {
            processed.put(field, "");
            processed.put(field + "1", "");
            processed.put(StrUtil.toUnderlineCase(field) + "1", "");
        }

        if (rawData == null) return processed;

        for (Map.Entry<String, Object> entry : rawData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 清洗 N/A、None 为空字符串
            String valStr = "";
            if (value != null
                    && !"N/A".equalsIgnoreCase(value.toString())
                    && !"None".equalsIgnoreCase(value.toString())) {
                valStr = value.toString().trim();
            }

            Object finalVal = isNumericField(key) ? cleanNumeric(valStr) : valStr;
            String snakeKey = StrUtil.toUnderlineCase(key);

            processed.put(key, finalVal);
            processed.put(key + "1", finalVal);
            processed.put(snakeKey + "1", finalVal);

            // 特殊字段映射（Word 模版占位符别名）
            applySpecialMappings(processed, key, finalVal);
        }

        // 运费条款自动补全 "AS ARRANGED"
        autoFillFreightArranged(processed);

        return processed;
    }

    private static void applySpecialMappings(Map<String, Object> data, String key, Object val) {
        if ("preCarriageBy".equalsIgnoreCase(key))  data.put("PRE-CARRIAGE BY", val);
        if ("carrierAgent".equalsIgnoreCase(key))   data.put("Carrier Agent1", val);
        if ("serviceMode".equalsIgnoreCase(key))    data.put("service-mode1", val);
        if ("collectAmount".equalsIgnoreCase(key))  data.put("collect_type1", val);
        if ("containerNo".equalsIgnoreCase(key))    data.put("container_number1", val);
        if ("sealNo".equalsIgnoreCase(key))         data.put("seal_number1", val);
        if ("containerWeight".equalsIgnoreCase(key))data.put("container_weight1", val);
        if ("vgmWeight".equalsIgnoreCase(key))      data.put("VGM", val);
    }

    private static void autoFillFreightArranged(Map<String, Object> data) {
        String term = String.valueOf(data.getOrDefault("freightTerm", "")).toUpperCase();
        if (term.contains("PREPAID")) {
            data.putIfAbsent("prepaidAmount", "AS ARRANGED");
            data.put("prepaidAmount1", "AS ARRANGED");
        }
        if (term.contains("COLLECT")) {
            data.putIfAbsent("collectAmount", "AS ARRANGED");
            data.put("collectAmount1", "AS ARRANGED");
            data.put("collect_type1", "AS ARRANGED");
        }
    }

    private static boolean isNumericField(String key) {
        String k = key.toLowerCase();
        return k.contains("weight") || k.contains("measurement")
                || k.contains("amount") || k.contains("quantity") || k.contains("rate");
    }

    private static String cleanNumeric(String val) {
        if (StrUtil.isEmpty(val)) return "";
        String cleaned = val.replaceAll("[^0-9.]", "");
        if (StrUtil.isEmpty(cleaned)) return "";
        int first = cleaned.indexOf('.');
        if (first != cleaned.lastIndexOf('.')) {
            cleaned = cleaned.substring(0, first + 1) + cleaned.substring(first + 1).replace(".", "");
        }
        return cleaned;
    }
}

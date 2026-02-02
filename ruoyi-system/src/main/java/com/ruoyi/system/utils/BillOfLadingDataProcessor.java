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
        if (rawData == null) {
            return new HashMap<>();
        }

        Map<String, Object> processed = new HashMap<>();

        for (Map.Entry<String, Object> entry : rawData.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();

            // 1. 处理 "N/A" 或 null
            if (value == null || "N/A".equalsIgnoreCase(value.toString())
                    || "None".equalsIgnoreCase(value.toString())) {
                processed.put(key, "");
                continue;
            }

            String valStr = value.toString().trim();

            // 2. 针对特定字段进行预处理
            if (isNumericField(key)) {
                processed.put(key, cleanNumeric(valStr));
            } else if (isMultilineField(key)) {
                // 保留换行符，poi-tl 默认支持换行
                processed.put(key, valStr);
            } else {
                processed.put(key, valStr);
            }
        }

        return processed;
    }

    /**
     * 判断是否为数值字段
     */
    private static boolean isNumericField(String key) {
        return key.contains("weight") || key.contains("measurement") || key.contains("tons")
                || key.contains("amount") || key.contains("quantity") || key.contains("rate");
    }

    /**
     * 判断是否为多行文本字段
     */
    private static boolean isMultilineField(String key) {
        return "shipper".equalsIgnoreCase(key) || "consignee".equalsIgnoreCase(key)
                || "notify_party".equalsIgnoreCase(key) || "goods_description".equalsIgnoreCase(key)
                || "marks".equalsIgnoreCase(key);
    }

    /**
     * 清洗数值字符串，去掉单位，保留纯数字
     */
    private static String cleanNumeric(String val) {
        if (StringUtils.isEmpty(val)) {
            return "0.00";
        }
        // 去除所有非数字和非焦点的字符（保留点号）
        String cleaned = val.replaceAll("[^0-9.]", "");
        if (StringUtils.isEmpty(cleaned)) {
            return "0.00";
        }
        // 如果有多个点，只保留第一个
        if (cleaned.indexOf(".") != cleaned.lastIndexOf(".")) {
            int firstDot = cleaned.indexOf(".");
            cleaned = cleaned.substring(0, firstDot + 1) + cleaned.substring(firstDot + 1).replace(".", "");
        }
        return cleaned;
    }
}

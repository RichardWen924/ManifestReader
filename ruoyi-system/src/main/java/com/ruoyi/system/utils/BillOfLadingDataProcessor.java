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

            // 3. 映射到 Word 模板标签：数据库字段名 + "1"
            // 同时保留原键值对，并添加下划线格式 + "1" 的格式
            String snakeKey = toSnakeCase(originalKey);
            processed.put(originalKey, finalValue); // 原样保留
            processed.put(originalKey + "1", finalValue); // 原样 + 1
            processed.put(snakeKey + "1", finalValue); // 下划线 + 1
        }

        return processed;
    }

    /**
     * 驼峰转下划线 (优化版)
     */
    private static String toSnakeCase(String str) {
        if (str == null)
            return null;
        // 如果已经是下画线格式，直接返回
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
        return lowerKey.contains("weight") || lowerKey.contains("measurement") || lowerKey.contains("tons")
                || lowerKey.contains("amount") || lowerKey.contains("quantity") || lowerKey.contains("rate");
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

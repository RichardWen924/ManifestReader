package com.ruoyi.common.utils;

/**
 * 高并发业务单号生成器
 * 18位格式：前缀(4位) + 时间戳(12位yyMMddHHmmss) + 随机数(2位)
 * 
 * @author ruoyi
 */
public class DocNoGenerator {

    private static final java.time.format.DateTimeFormatter TIMESTAMP_FORMATTER = java.time.format.DateTimeFormatter
            .ofPattern("yyMMddHHmmss");
    private static final int PREFIX_LENGTH = 4;
    private static final java.util.Random RANDOM = new java.util.Random();

    /**
     * 生成下一个业务单号
     * 18位 = 前缀(4) + 时间戳(12: yyMMddHHmmss) + 随机数(2)
     * 
     * @param prefix 前缀（大写字母，不足4位补X，多于4位截取）
     * @return 18位单号
     */
    public static String nextDocNo(String prefix) {
        // 1. 处理前缀
        String finalPrefix = normalizePrefix(prefix);

        // 2. 获取当前时间戳 (12位)
        String timestamp = java.time.LocalDateTime.now().format(TIMESTAMP_FORMATTER);

        // 3. 生成2位随机数 (确保凑齐18位)
        int randomPart = RANDOM.nextInt(90) + 10; // 10-99

        return finalPrefix + timestamp + randomPart;
    }

    /**
     * 前缀规范化：转大写，补'X'或截取
     */
    private static String normalizePrefix(String prefix) {
        if (prefix == null || prefix.trim().isEmpty()) {
            return "XXXX";
        }
        // 提取字母和数字，转大写
        String p = prefix.replaceAll("[^a-zA-Z0-9]", "").toUpperCase();
        if (p.isEmpty())
            return "XXXX";

        if (p.length() < PREFIX_LENGTH) {
            StringBuilder sb = new StringBuilder(p);
            while (sb.length() < PREFIX_LENGTH) {
                sb.append('X');
            }
            return sb.toString();
        } else if (p.length() > PREFIX_LENGTH) {
            return p.substring(0, PREFIX_LENGTH);
        }
        return p;
    }
}

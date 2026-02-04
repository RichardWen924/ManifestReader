package com.ruoyi.common.utils;

/**
 * 高并发业务单号生成器
 * 14位格式：前缀(4位) + 年月(6位yyyyMM) + 序列号(4位)
 * 数字位共10位 (6位年月 + 4位序列)
 * 
 * @author ruoyi
 */
public class DocNoGenerator {

    private static final java.time.format.DateTimeFormatter MONTH_FORMATTER = java.time.format.DateTimeFormatter
            .ofPattern("yyyyMM");
    private static final java.util.concurrent.atomic.AtomicLong SEQUENCE = new java.util.concurrent.atomic.AtomicLong(
            0);
    private static volatile String lastMonth = "";
    private static final java.util.concurrent.locks.ReentrantLock LOCK = new java.util.concurrent.locks.ReentrantLock();

    /**
     * 生成下一个业务单号 (V6 更新规则)
     * 14位 = EVHL(4) + 年月(6: yyyyMM) + 序列号(4: 0001开始)
     * 数字位共10位 (6位年月 + 4位序列)
     * 
     * @param prefix 原始逻辑传参（新规则下强制使用 EVHL）
     * @return 14位单号
     */
    public static String nextDocNo(String prefix) {
        // 1. 获取当前年月 (6位)
        String currentMonth = java.time.LocalDateTime.now().format(MONTH_FORMATTER);

        // 2. 获取序列号 (支持按月重置)
        long seq = getNextSequence(currentMonth);

        // 3. 拼接结果: EVLH + yyyyMM + 4位序列 (4 + 6 + 4 = 14)
        return String.format("EVHL%s%04d", currentMonth, seq);
    }

    /**
     * 获取序列号，若月份变更则重置
     */
    private static long getNextSequence(String currentMonth) {
        if (!currentMonth.equals(lastMonth)) {
            LOCK.lock();
            try {
                if (!currentMonth.equals(lastMonth)) {
                    lastMonth = currentMonth;
                    SEQUENCE.set(0);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return SEQUENCE.incrementAndGet();
    }
}

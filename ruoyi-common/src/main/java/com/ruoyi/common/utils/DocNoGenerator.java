package com.ruoyi.common.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 高并发业务单号生成器
 * 18位格式：前缀(4位) + 日期(6位yyMMdd) + 流水号(8位)
 * 
 * @author ruoyi
 */
public class DocNoGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int PREFIX_LENGTH = 4;
    private static final int SEQ_LENGTH = 8;
    private static final long MAX_SEQ = 99999999L;

    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static volatile String lastDate = "";
    private static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * 生成下一个业务单号
     * 
     * @param prefix 前缀（大写字母，不足4位补X，多于4位截取）
     * @return 18位单号
     */
    public static String nextDocNo(String prefix) {
        // 1. 处理前缀
        String finalPrefix = normalizePrefix(prefix);

        // 2. 获取当前日期
        String currentDate = LocalDateTime.now().format(DATE_FORMATTER);

        // 3. 获取序列号（支持跨天重置）
        long seq = getNextSequence(currentDate);

        // 4. 拼接结果 (4 + 6 + 8 = 18)
        return String.format("%s%s%08d", finalPrefix, currentDate, seq);
    }

    /**
     * 前缀规范化：转大写，补'X'或截取
     */
    private static String normalizePrefix(String prefix) {
        if (prefix == null) {
            return "XXXX";
        }
        String p = prefix.trim().toUpperCase();
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

    /**
     * 获取序列号，若日期变更则重置
     */
    private static long getNextSequence(String currentDate) {
        // 双检锁确保日期变更时原子重置
        if (!currentDate.equals(lastDate)) {
            LOCK.lock();
            try {
                if (!currentDate.equals(lastDate)) {
                    lastDate = currentDate;
                    SEQUENCE.set(0);
                }
            } finally {
                LOCK.unlock();
            }
        }

        long next = SEQUENCE.incrementAndGet();
        if (next > MAX_SEQ) {
            LOCK.lock();
            try {
                // 如果超过最大流水号，强制循环（实际每天1亿单概率极低）
                if (SEQUENCE.get() > MAX_SEQ) {
                    SEQUENCE.set(1);
                    return 1;
                }
            } finally {
                LOCK.unlock();
            }
            return SEQUENCE.incrementAndGet();
        }
        return next;
    }
}

package com.ruoyi.common.utils;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

/**
 * 提单号生成器
 * 14位格式：前缀(4位字母) + 10位数字
 * 10位数字组成：公司编号前四位(4位) + 自增序列(6位)
 */
public class DocNoGenerator {

    private static final AtomicLong SEQUENCE = new AtomicLong(0);
    private static volatile String lastKey = "";
    private static final ReentrantLock LOCK = new ReentrantLock();

    /**
     * 生成下一个提单号
     * 
     * @param abbr        4位字母缩写
     * @param companyCode 公司编号 (YYMMDDNN)
     * @return 14位提单号
     */
    public static String nextDocNo(String abbr, String companyCode) {
        String prefix = (abbr != null && abbr.length() == 4) ? abbr.toUpperCase() : "EVHL";

        // 获取公司编号前四位 (通常是注册年月 YYMM)
        String yymm = (companyCode != null && companyCode.length() >= 4) ? companyCode.substring(0, 4) : "2601";

        String currentKey = prefix + "_" + yymm;

        long seq = getNextSequence(currentKey);

        // 格式: PREFIX(4) + YYMM(4) + SEQ(6) = 14位
        return String.format("%s%s%06d", prefix, yymm, seq);
    }

    private static long getNextSequence(String key) {
        if (!key.equals(lastKey)) {
            LOCK.lock();
            try {
                if (!key.equals(lastKey)) {
                    lastKey = key;
                    SEQUENCE.set(0);
                }
            } finally {
                LOCK.unlock();
            }
        }
        return SEQUENCE.incrementAndGet();
    }
}

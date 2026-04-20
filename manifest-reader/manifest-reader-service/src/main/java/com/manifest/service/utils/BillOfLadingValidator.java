package com.manifest.service.utils;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import java.util.Map;

/**
 * 提单业务规则校验器
 * 在 PDF 填充和数据入库前执行所有业务规则
 */
@Slf4j
public class BillOfLadingValidator {

    public static Map<String, Object> applyBusinessRules(Map<String, Object> data) {
        if (data == null || data.isEmpty()) return data;
        syncBlNoAndBookingNo(data);
        processFreightTermLogic(data);
        autoAppendWeightUnit(data);
        return data;
    }

    /** 规则1: blNo 强制覆盖 bookingNo */
    private static void syncBlNoAndBookingNo(Map<String, Object> data) {
        String blNo = str(data, "blNo");
        String bookingNo = str(data, "bookingNo");
        if (StrUtil.isNotEmpty(blNo) && !blNo.equals(bookingNo)) {
            log.warn("[Validator] blNo({}) 与 bookingNo({}) 不一致，强制同步", blNo, bookingNo);
            data.put("bookingNo", blNo);
        }
    }

    /** 规则2: 运费条款自动设置 AS ARRANGED */
    private static void processFreightTermLogic(Map<String, Object> data) {
        String term = str(data, "freightTerm");
        if (StrUtil.isEmpty(term)) return;
        if (term.toUpperCase().contains("COLLECT")) {
            if (StrUtil.isEmpty(str(data, "collectAmount"))) data.put("collectAmount", "AS ARRANGED");
            data.put("prepaidAmount", null);
        } else if (term.toUpperCase().contains("PREPAID")) {
            if (StrUtil.isEmpty(str(data, "prepaidAmount"))) data.put("prepaidAmount", "AS ARRANGED");
            data.put("collectAmount", null);
        }
    }

    /** 规则3: 重量字段自动追加 KGS 单位 */
    private static void autoAppendWeightUnit(Map<String, Object> data) {
        for (String field : new String[]{"grossWeight", "grossWeightKgs", "cargoGrossWeight"}) {
            String w = str(data, field);
            if (StrUtil.isNotEmpty(w) && !w.toUpperCase().contains("KGS")) {
                data.put(field, w + " KGS");
            }
        }
    }

    private static String str(Map<String, Object> data, String key) {
        Object v = data.get(key);
        return v == null ? null : v.toString().trim();
    }
}

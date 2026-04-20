package com.manifest.model.dto;

import lombok.Data;
import java.util.Map;

/**
 * AI 解析结果 DTO
 * 用于在 Dify 解析完成后，在 Redis 缓存和前端确认之间传递数据
 */
@Data
public class BillOfLadingDto {

    /** Redis缓存Key后缀，用于前端提交确认 */
    private String uuid;

    /** AI 提取并清洗后的业务数据 (驼峰命名) */
    private Map<String, Object> businessData;

    /** PDF 模版字段坐标配置 */
    private Map<String, FieldLocation> fieldLocations;

    /**
     * PDF 字段坐标信息
     */
    @Data
    public static class FieldLocation {
        /** 所在页码 */
        private int page;
        /** X 坐标（左下角原点） */
        private float x;
        /** Y 坐标 */
        private float y;
        /** 宽度 */
        private float w;
        /** 高度 */
        private float h;

        public FieldLocation(int page, float x, float y, float w, float h) {
            this.page = page;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }
    }
}

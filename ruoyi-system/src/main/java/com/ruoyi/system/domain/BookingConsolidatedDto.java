package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Map;

/**
 * 订舱与集装箱合并信息 DTO
 * 用于 Dify 识别回显及 PDF 坐标暂存
 */
public class BookingConsolidatedDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 暂存唯一标识 (Redis Key) */
    private String uuid;

    /** 业务数据 (对应实体类字段) */
    private Object businessData;

    /** 字段坐标信息 (FieldName -> Location) */
    private Map<String, FieldLocation> fieldLocations;

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public Object getBusinessData() { return businessData; }
    public void setBusinessData(Object businessData) { this.businessData = businessData; }

    public Map<String, FieldLocation> getFieldLocations() { return fieldLocations; }
    public void setFieldLocations(Map<String, FieldLocation> fieldLocations) { this.fieldLocations = fieldLocations; }

    /**
     * 坐标位置内部类
     */
    public static class FieldLocation implements Serializable {
        private float x;
        private float y;
        private float w;
        private float h;
        private int page;

        public float getX() { return x; }
        public void setX(float x) { this.x = x; }

        public float getY() { return y; }
        public void setY(float y) { this.y = y; }

        public float getW() { return w; }
        public void setW(float w) { this.w = w; }

        public float getH() { return h; }
        public void setH(float h) { this.h = h; }

        public int getPage() { return page; }
        public void setPage(int page) { this.page = page; }
    }
}

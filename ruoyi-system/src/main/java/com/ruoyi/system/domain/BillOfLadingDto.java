package com.ruoyi.system.domain;

import java.io.Serializable;
import java.util.Map;

/**
 * 提单数据传输对象
 * 用于前后端交互和AI分析结果
 */
public class BillOfLadingDto implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 会话UUID */
    private String uuid;

    /** 业务数据（包含所有字段） */
    private Object businessData;

    /** 字段位置信息（用于PDF覆盖） */
    private Map<String, FieldLocation> fieldLocations;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Object getBusinessData() {
        return businessData;
    }

    public void setBusinessData(Object businessData) {
        this.businessData = businessData;
    }

    public Map<String, FieldLocation> getFieldLocations() {
        return fieldLocations;
    }

    public void setFieldLocations(Map<String, FieldLocation> fieldLocations) {
        this.fieldLocations = fieldLocations;
    }

    /**
     * 字段位置信息（PDF坐标）
     */
    public static class FieldLocation implements Serializable {
        private static final long serialVersionUID = 1L;

        private int page; // 页码（从1开始）
        private float x; // X坐标
        private float y; // Y坐标
        private float w; // 宽度
        private float h; // 高度

        public FieldLocation() {
        }

        public FieldLocation(int page, float x, float y, float w, float h) {
            this.page = page;
            this.x = x;
            this.y = y;
            this.w = w;
            this.h = h;
        }

        public int getPage() {
            return page;
        }

        public void setPage(int page) {
            this.page = page;
        }

        public float getX() {
            return x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return y;
        }

        public void setY(float y) {
            this.y = y;
        }

        public float getW() {
            return w;
        }

        public void setW(float w) {
            this.w = w;
        }

        public float getH() {
            return h;
        }

        public void setH(float h) {
            this.h = h;
        }
    }
}

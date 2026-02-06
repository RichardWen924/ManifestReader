package com.ruoyi.system.domain;

import com.alibaba.fastjson.annotation.JSONField;

/**
 * 模版映射对象
 */
public class SysTemplateMapping {
    @JSONField(name = "original_text")
    private String originalText;

    @JSONField(name = "placeholder_key")
    private String placeholderKey;

    @JSONField(name = "data_type")
    private String dataType;

    private String description;

    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    public String getPlaceholderKey() {
        return placeholderKey;
    }

    public void setPlaceholderKey(String placeholderKey) {
        this.placeholderKey = placeholderKey;
    }

    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

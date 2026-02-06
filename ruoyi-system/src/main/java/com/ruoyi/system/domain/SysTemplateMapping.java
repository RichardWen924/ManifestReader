package com.ruoyi.system.domain;

import com.alibaba.fastjson.annotation.JSONField;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * 模版映射对象
 */
public class SysTemplateMapping {
    @JSONField(name = "original_text")
    @JsonProperty("original_text")
    private String originalText;

    @JSONField(name = "placeholder_key")
    @JsonProperty("placeholder_key")
    private String placeholderKey;

    @JSONField(name = "data_type")
    @JsonProperty("data_type")
    private String dataType;

    private String description;

    @JsonProperty("original_text")
    public String getOriginalText() {
        return originalText;
    }

    public void setOriginalText(String originalText) {
        this.originalText = originalText;
    }

    @JsonProperty("placeholder_key")
    public String getPlaceholderKey() {
        return placeholderKey;
    }

    public void setPlaceholderKey(String placeholderKey) {
        this.placeholderKey = placeholderKey;
    }

    @JsonProperty("data_type")
    public String getDataType() {
        return dataType;
    }

    public void setDataType(String dataType) {
        this.dataType = dataType;
    }

    @JsonProperty("description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}

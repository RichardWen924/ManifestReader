package com.manifest.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 提单费用与签发信息表 bl_freight
 */
@Data
@TableName("bl_freight")
public class BlFreight {

    @TableId
    private Long blId;

    /** 服务类型 */
    private String serviceType;

    /** 服务模式 */
    private String serviceMode;

    /** 计费吨 */
    private String revenueTons;

    /** 运费条款 (Prepaid/Collect) */
    private String freightTerm;

    /** 费率 */
    private String freightRate;

    /** 预付金额 */
    private String prepaidAmount;

    /** 到付金额 */
    private String collectAmount;

    /** 付款地点 */
    private String payableAt;

    /** 正本份数 */
    private String originalBlCount;

    /** 签发地 */
    private String issuePlace;

    /** 装船日期/说明 */
    private String ladenOnBoard;
}

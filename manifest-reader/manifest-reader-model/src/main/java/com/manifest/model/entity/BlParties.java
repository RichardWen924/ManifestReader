package com.manifest.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 提单参与方表 bl_parties
 */
@Data
@TableName("bl_parties")
public class BlParties {

    @TableId
    private Long blId;

    /** 发货人 */
    private String shipper;

    /** 收货人 */
    private String consignee;

    /** 通知方 */
    private String notifyParty;

    /** 承运代理 */
    private String carrierAgent;

    /** 交付代理 */
    private String deliveryAgent;
}

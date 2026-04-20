package com.manifest.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.manifest.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 提单主表 bl_main
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bl_main")
public class BillOfLading extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提单号 */
    private String blNo;

    /** 订舱号 */
    private String bookingNo;

    /** 文件编号 */
    private String docNo;

    /** 序列号 */
    private String serialNo;

    /** 船名航次 */
    private String vesselVoyage;

    /** 前段运输 */
    private String preCarriageBy;

    /** 收货地 */
    private String placeOfReceipt;

    /** 装货港 */
    private String portOfLoading;

    /** 卸货港 */
    private String portOfDischarge;

    /** 交货地 */
    private String placeOfDelivery;

    /** 生成的PDF文件路径 */
    private String filePath;

    /** 参与方（非DB字段，级联查询填充） */
    @TableField(exist = false)
    private BlParties parties;

    /** 货物明细（非DB字段，级联查询填充） */
    @TableField(exist = false)
    private BlCargo cargo;

    /** 费用信息（非DB字段，级联查询填充） */
    @TableField(exist = false)
    private BlFreight freight;
}

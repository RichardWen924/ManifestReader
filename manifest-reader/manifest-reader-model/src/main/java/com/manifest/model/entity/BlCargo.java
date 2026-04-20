package com.manifest.model.entity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

/**
 * 提单货物明细表 bl_cargo
 */
@Data
@TableName("bl_cargo")
public class BlCargo {

    @TableId
    private Long blId;

    /** 集装箱号 */
    private String containerNo;

    /** 封号 */
    private String sealNo;

    /** 箱重 */
    private BigDecimal containerWeight;

    /** VGM核实总重 */
    private BigDecimal vgmWeight;

    /** 包装件数 */
    private Integer packageQuantity;

    /** 包装单位 */
    private String packageUnit;

    /** 货物描述 */
    private String goodsDescription;

    /** 总重(KGS) */
    private BigDecimal grossWeightKgs;

    /** 体积(CBM) */
    private BigDecimal measurementCbm;

    /** 集装箱封条综合信息 */
    private String containerSealInfo;

    /** 唛头 */
    private String marks;
}

package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;
import java.math.BigDecimal;

/**
 * 提单货物明细对象 bl_cargo
 * 
 * @author Richard
 */
@TableName("bl_cargo")
public class BlCargo implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 关联主表ID */
    @TableId(type = IdType.INPUT)
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

    /** 总重 (KGS) */
    private BigDecimal grossWeightKgs;

    /** 体积 (CBM) */
    private BigDecimal measurementCbm;

    /** 集装箱封条综合信息 */
    private String containerSealInfo;

    /** 唛头 */
    private String marks;

    public Long getBlId() { return blId; }
    public void setBlId(Long blId) { this.blId = blId; }

    public String getContainerNo() { return containerNo; }
    public void setContainerNo(String containerNo) { this.containerNo = containerNo; }

    public String getSealNo() { return sealNo; }
    public void setSealNo(String sealNo) { this.sealNo = sealNo; }

    public BigDecimal getContainerWeight() { return containerWeight; }
    public void setContainerWeight(BigDecimal containerWeight) { this.containerWeight = containerWeight; }

    public BigDecimal getVgmWeight() { return vgmWeight; }
    public void setVgmWeight(BigDecimal vgmWeight) { this.vgmWeight = vgmWeight; }

    public Integer getPackageQuantity() { return packageQuantity; }
    public void setPackageQuantity(Integer packageQuantity) { this.packageQuantity = packageQuantity; }

    public String getPackageUnit() { return packageUnit; }
    public void setPackageUnit(String packageUnit) { this.packageUnit = packageUnit; }

    public String getGoodsDescription() { return goodsDescription; }
    public void setGoodsDescription(String goodsDescription) { this.goodsDescription = goodsDescription; }

    public BigDecimal getGrossWeightKgs() { return grossWeightKgs; }
    public void setGrossWeightKgs(BigDecimal grossWeightKgs) { this.grossWeightKgs = grossWeightKgs; }

    public BigDecimal getMeasurementCbm() { return measurementCbm; }
    public void setMeasurementCbm(BigDecimal measurementCbm) { this.measurementCbm = measurementCbm; }

    public String getContainerSealInfo() { return containerSealInfo; }
    public void setContainerSealInfo(String containerSealInfo) { this.containerSealInfo = containerSealInfo; }

    public String getMarks() { return marks; }
    public void setMarks(String marks) { this.marks = marks; }

    @Override
    public String toString() {
        return "BlCargo{" +
                "blId=" + blId +
                ", containerNo='" + containerNo + '\'' +
                ", sealNo='" + sealNo + '\'' +
                '}';
    }
}

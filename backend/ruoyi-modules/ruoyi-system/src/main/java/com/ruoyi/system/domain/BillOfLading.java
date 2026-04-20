package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.IdType;

/**
 * 提单信息对象 bill_of_lading
 * 
 * @author Richard
 * @date 2026-01-29
 */
@TableName("bl_main")
public class BillOfLading extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** ID */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 提单号 */
    @Excel(name = "提单号")
    private String blNo;

    /** 订舱号 */
    @Excel(name = "订舱号")
    private String bookingNo;

    /** 文件编号 */
    @Excel(name = "文件编号")
    private String docNo;

    /** 序列号 */
    @Excel(name = "序列号")
    private String serialNo;

    /** 船名航次 */
    @Excel(name = "船名航次")
    private String vesselVoyage;

    /** 前段运输 */
    @Excel(name = "前段运输")
    private String preCarriageBy;

    /** 收货地 */
    @Excel(name = "收货地")
    private String placeOfReceipt;

    /** 装货港 */
    @Excel(name = "装货港")
    private String portOfLoading;

    /** 卸货港 */
    @Excel(name = "卸货港")
    private String portOfDischarge;

    /** 交货地 */
    @Excel(name = "交货地")
    private String placeOfDelivery;

    /** PDF文件路径 */
    private String filePath;

    /** 参与方信息 */
    @TableField(exist = false)
    private BlParties parties = new BlParties();

    /** 货物明细 */
    @TableField(exist = false)
    private BlCargo cargo = new BlCargo();

    /** 费用与签发 */
    @TableField(exist = false)
    private BlFreight freight = new BlFreight();

    // --- 兼容旧代码的代理方法 ---

    public void setId(Long id) { this.id = id; }
    public Long getId() { return id; }
    public void setBlNo(String blNo) { this.blNo = blNo; }
    public String getBlNo() { return blNo; }
    public void setBookingNo(String bookingNo) { this.bookingNo = bookingNo; }
    public String getBookingNo() { return bookingNo; }
    public void setDocNo(String docNo) { this.docNo = docNo; }
    public String getDocNo() { return docNo; }
    public void setSerialNo(String serialNo) { this.serialNo = serialNo; }
    public String getSerialNo() { return serialNo; }
    public void setVesselVoyage(String vesselVoyage) { this.vesselVoyage = vesselVoyage; }
    public String getVesselVoyage() { return vesselVoyage; }
    public void setPreCarriageBy(String preCarriageBy) { this.preCarriageBy = preCarriageBy; }
    public String getPreCarriageBy() { return preCarriageBy; }
    public void setPlaceOfReceipt(String placeOfReceipt) { this.placeOfReceipt = placeOfReceipt; }
    public String getPlaceOfReceipt() { return placeOfReceipt; }
    public void setPortOfLoading(String portOfLoading) { this.portOfLoading = portOfLoading; }
    public String getPortOfLoading() { return portOfLoading; }
    public void setPortOfDischarge(String portOfDischarge) { this.portOfDischarge = portOfDischarge; }
    public String getPortOfDischarge() { return portOfDischarge; }
    public void setPlaceOfDelivery(String placeOfDelivery) { this.placeOfDelivery = placeOfDelivery; }
    public String getPlaceOfDelivery() { return placeOfDelivery; }
    public void setFilePath(String filePath) { this.filePath = filePath; }
    public String getFilePath() { return filePath; }

    @Excel(name = "发货人")
    public String getShipper() { return parties.getShipper(); }
    public void setShipper(String shipper) { parties.setShipper(shipper); }
    @Excel(name = "收货人")
    public String getConsignee() { return parties.getConsignee(); }
    public void setConsignee(String consignee) { parties.setConsignee(consignee); }
    @Excel(name = "通知方")
    public String getNotifyParty() { return parties.getNotifyParty(); }
    public void setNotifyParty(String notifyParty) { parties.setNotifyParty(notifyParty); }
    @Excel(name = "承运代理")
    public String getCarrierAgent() { return parties.getCarrierAgent(); }
    public void setCarrierAgent(String carrierAgent) { parties.setCarrierAgent(carrierAgent); }
    @Excel(name = "交付申请方")
    public String getDeliveryAgent() { return parties.getDeliveryAgent(); }
    public void setDeliveryAgent(String deliveryAgent) { parties.setDeliveryAgent(deliveryAgent); }

    @Excel(name = "集装箱号")
    public String getContainerNo() { return cargo.getContainerNo(); }
    public void setContainerNo(String containerNo) { cargo.setContainerNo(containerNo); }
    @Excel(name = "封号")
    public String getSealNo() { return cargo.getSealNo(); }
    public void setSealNo(String sealNo) { cargo.setSealNo(sealNo); }
    @Excel(name = "箱重")
    public BigDecimal getContainerWeight() { return cargo.getContainerWeight(); }
    public void setContainerWeight(BigDecimal containerWeight) { cargo.setContainerWeight(containerWeight); }
    @Excel(name = "VGM核实总重")
    public BigDecimal getVgmWeight() { return cargo.getVgmWeight(); }
    public void setVgmWeight(BigDecimal vgmWeight) { cargo.setVgmWeight(vgmWeight); }
    @Excel(name = "包装件数")
    public Integer getPackageQuantity() { return cargo.getPackageQuantity(); }
    public void setPackageQuantity(Integer packageQuantity) { cargo.setPackageQuantity(packageQuantity); }
    @Excel(name = "包装单位")
    public String getPackageUnit() { return cargo.getPackageUnit(); }
    public void setPackageUnit(String packageUnit) { cargo.setPackageUnit(packageUnit); }
    @Excel(name = "货物描述")
    public String getGoodsDescription() { return cargo.getGoodsDescription(); }
    public void setGoodsDescription(String goodsDescription) { cargo.setGoodsDescription(goodsDescription); }
    @Excel(name = "总重(KG)")
    public BigDecimal getGrossWeightKgs() { return cargo.getGrossWeightKgs(); }
    public void setGrossWeightKgs(BigDecimal grossWeightKgs) { cargo.setGrossWeightKgs(grossWeightKgs); }
    @Excel(name = "体积(CBM)")
    public BigDecimal getMeasurementCbm() { return cargo.getMeasurementCbm(); }
    public void setMeasurementCbm(BigDecimal measurementCbm) { cargo.setMeasurementCbm(measurementCbm); }
    @Excel(name = "集装箱与封条号")
    public String getContainerSealInfo() { return cargo.getContainerSealInfo(); }
    public void setContainerSealInfo(String containerSealInfo) { cargo.setContainerSealInfo(containerSealInfo); }
    @Excel(name = "唛头")
    public String getMarks() { return cargo.getMarks(); }
    public void setMarks(String marks) { cargo.setMarks(marks); }

    @Excel(name = "服务类型")
    public String getServiceType() { return freight.getServiceType(); }
    public void setServiceType(String serviceType) { freight.setServiceType(serviceType); }
    @Excel(name = "服务模式")
    public String getServiceMode() { return freight.getServiceMode(); }
    public void setServiceMode(String serviceMode) { freight.setServiceMode(serviceMode); }
    @Excel(name = "计费吨")
    public String getRevenueTons() { return freight.getRevenueTons(); }
    public void setRevenueTons(String revenueTons) { freight.setRevenueTons(revenueTons); }
    @Excel(name = "运费条款")
    public String getFreightTerm() { return freight.getFreightTerm(); }
    public void setFreightTerm(String freightTerm) { freight.setFreightTerm(freightTerm); }
    @Excel(name = "运费费率")
    public String getFreightRate() { return freight.getFreightRate(); }
    public void setFreightRate(String freightRate) { freight.setFreightRate(freightRate); }
    @Excel(name = "预付金额")
    public String getPrepaidAmount() { return freight.getPrepaidAmount(); }
    public void setPrepaidAmount(String prepaidAmount) { freight.setPrepaidAmount(prepaidAmount); }
    @Excel(name = "到付金额")
    public String getCollectAmount() { return freight.getCollectAmount(); }
    public void setCollectAmount(String collectAmount) { freight.setCollectAmount(collectAmount); }
    @Excel(name = "付款地点")
    public String getPayableAt() { return freight.getPayableAt(); }
    public void setPayableAt(String payableAt) { freight.setPayableAt(payableAt); }
    @Excel(name = "正本份数")
    public String getOriginalBlCount() { return freight.getOriginalBlCount(); }
    public void setOriginalBlCount(String originalBlCount) { freight.setOriginalBlCount(originalBlCount); }
    @Excel(name = "签发地点")
    public String getIssuePlace() { return freight.getIssuePlace(); }
    public void setIssuePlace(String issuePlace) { freight.setIssuePlace(issuePlace); }
    @Excel(name = "装船信息")
    public String getLadenOnBoard() { return freight.getLadenOnBoard(); }
    public void setLadenOnBoard(String ladenOnBoard) { freight.setLadenOnBoard(ladenOnBoard); }

    // --- 组件的 Getter/Setter (MyBatis映射使用) ---
    public BlParties getParties() { return parties; }
    public void setParties(BlParties parties) { this.parties = parties; }
    public BlCargo getCargo() { return cargo; }
    public void setCargo(BlCargo cargo) { this.cargo = cargo; }
    public BlFreight getFreight() { return freight; }
    public void setFreight(BlFreight freight) { this.freight = freight; }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("blNo", getBlNo())
                .append("bookingNo", getBookingNo())
                .append("parties", getParties())
                .append("cargo", getCargo())
                .append("freight", getFreight())
                .toString();
    }
}

}

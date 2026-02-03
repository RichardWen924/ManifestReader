package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订舱与集装箱合并信息对象 booking_consolidated (V5 版本)
 * 
 * @author ruoyi
 * @date 2026-02-02
 */
public class BookingConsolidated extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 主键ID */
    private Long id;

    /** B/L NO. */
    @Excel(name = "B/L NO.")
    private String blNo;

    /** 订舱编号 (BOOKING NO.) */
    @Excel(name = "订舱编号")
    private String bookingNo;

    /** DOC. NO. */
    @Excel(name = "DOC. NO.")
    private String docNo;

    /** SERIAL NO. */
    @Excel(name = "SERIAL NO.")
    private String serialNo;

    /** 发货人信息 */
    @Excel(name = "发货人信息")
    private String shipper;

    /** 收货人信息 */
    @Excel(name = "收货人信息")
    private String consignee;

    /** 通知方信息 */
    @Excel(name = "通知方信息")
    private String notifyParty;

    /** 承运人代理 */
    @Excel(name = "承运人代理")
    private String carrierAgent;

    /** 交货代理 */
    @Excel(name = "交货代理")
    private String deliveryAgent;

    /** 前段运输 */
    @Excel(name = "前段运输")
    private String preCarriageBy;

    /** 船名航次 */
    @Excel(name = "船名航次")
    private String vesselVoyage;

    /** 船名 */
    @Excel(name = "船名")
    private String vesselName;

    /** 航次 */
    @Excel(name = "航次")
    private String voyageNo;

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

    /** 集装箱/封条信息 */
    @Excel(name = "集装箱/封条")
    private String containerSealInfo;

    /** 箱号 */
    @Excel(name = "箱号")
    private String containerNo;

    /** 封号 */
    @Excel(name = "封号")
    private String sealNo;

    /** 箱重 */
    @Excel(name = "箱重")
    private BigDecimal containerWeight;

    /** VGM 核实总重 */
    @Excel(name = "VGM核实总重")
    private BigDecimal vgmWeight;

    /** 包装数量 (件数+单位) */
    @Excel(name = "包装数量")
    private String packageQuantity;

    /** 包装单位 */
    @Excel(name = "包装单位")
    private String packageUnit;

    /** 货物描述 */
    @Excel(name = "货物描述")
    private String goodsDescription;

    /** 唛头 */
    @Excel(name = "唛头")
    private String marks;

    /** 毛重(KGS) */
    @Excel(name = "毛重(KGS)")
    private BigDecimal grossWeightKgs;

    /** 体积(CBM) */
    @Excel(name = "体积(CBM)")
    private BigDecimal measurementCbm;

    /** 服务类型 */
    @Excel(name = "服务类型")
    private String serviceType;

    /** 服务模式 */
    @Excel(name = "服务模式")
    private String serviceMode;

    /** 计费吨 */
    @Excel(name = "计费吨")
    private String revenueTons;

    /** 费用条款 */
    @Excel(name = "费用条款")
    private String freightTerm;

    /** 费率 */
    @Excel(name = "费率")
    private String freightRate;

    /** 预付金额 */
    @Excel(name = "预付")
    private String prepaidAmount;

    /** 到付金额 */
    @Excel(name = "到付")
    private String collectAmount;

    /** 支付地 */
    @Excel(name = "支付地")
    private String payableAt;

    /** 正本份数 */
    @Excel(name = "正本份数")
    private String originalBlCount;

    /** 签发地 */
    @Excel(name = "签发地")
    private String issuePlace;

    /** 装船批注 */
    @Excel(name = "装船批注")
    private String ladenOnBoard;

    /** 文件存储路径 */
    @Excel(name = "文件存储路径")
    private String filePath;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+8")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date createdAt;

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBlNo() {
        return blNo;
    }

    public void setBlNo(String blNo) {
        this.blNo = blNo;
    }

    public String getBookingNo() {
        return bookingNo;
    }

    public void setBookingNo(String bookingNo) {
        this.bookingNo = bookingNo;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getShipper() {
        return shipper;
    }

    public void setShipper(String shipper) {
        this.shipper = shipper;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public String getNotifyParty() {
        return notifyParty;
    }

    public void setNotifyParty(String notifyParty) {
        this.notifyParty = notifyParty;
    }

    public String getCarrierAgent() {
        return carrierAgent;
    }

    public void setCarrierAgent(String carrierAgent) {
        this.carrierAgent = carrierAgent;
    }

    public String getDeliveryAgent() {
        return deliveryAgent;
    }

    public void setDeliveryAgent(String deliveryAgent) {
        this.deliveryAgent = deliveryAgent;
    }

    public String getPreCarriageBy() {
        return preCarriageBy;
    }

    public void setPreCarriageBy(String preCarriageBy) {
        this.preCarriageBy = preCarriageBy;
    }

    public String getVesselVoyage() {
        return vesselVoyage;
    }

    public void setVesselVoyage(String vesselVoyage) {
        this.vesselVoyage = vesselVoyage;
    }

    public String getVesselName() {
        return vesselName;
    }

    public void setVesselName(String vesselName) {
        this.vesselName = vesselName;
    }

    public String getVoyageNo() {
        return voyageNo;
    }

    public void setVoyageNo(String voyageNo) {
        this.voyageNo = voyageNo;
    }

    public String getPlaceOfReceipt() {
        return placeOfReceipt;
    }

    public void setPlaceOfReceipt(String placeOfReceipt) {
        this.placeOfReceipt = placeOfReceipt;
    }

    public String getPortOfLoading() {
        return portOfLoading;
    }

    public void setPortOfLoading(String portOfLoading) {
        this.portOfLoading = portOfLoading;
    }

    public String getPortOfDischarge() {
        return portOfDischarge;
    }

    public void setPortOfDischarge(String portOfDischarge) {
        this.portOfDischarge = portOfDischarge;
    }

    public String getPlaceOfDelivery() {
        return placeOfDelivery;
    }

    public void setPlaceOfDelivery(String placeOfDelivery) {
        this.placeOfDelivery = placeOfDelivery;
    }

    public String getContainerSealInfo() {
        return containerSealInfo;
    }

    public void setContainerSealInfo(String containerSealInfo) {
        this.containerSealInfo = containerSealInfo;
    }

    public String getContainerNo() {
        return containerNo;
    }

    public void setContainerNo(String containerNo) {
        this.containerNo = containerNo;
    }

    public String getSealNo() {
        return sealNo;
    }

    public void setSealNo(String sealNo) {
        this.sealNo = sealNo;
    }

    public BigDecimal getContainerWeight() {
        return containerWeight;
    }

    public void setContainerWeight(BigDecimal containerWeight) {
        this.containerWeight = containerWeight;
    }

    public BigDecimal getVgmWeight() {
        return vgmWeight;
    }

    public void setVgmWeight(BigDecimal vgmWeight) {
        this.vgmWeight = vgmWeight;
    }

    public String getPackageQuantity() {
        return packageQuantity;
    }

    public void setPackageQuantity(String packageQuantity) {
        this.packageQuantity = packageQuantity;
    }

    public String getPackageUnit() {
        return packageUnit;
    }

    public void setPackageUnit(String packageUnit) {
        this.packageUnit = packageUnit;
    }

    public String getGoodsDescription() {
        return goodsDescription;
    }

    public void setGoodsDescription(String goodsDescription) {
        this.goodsDescription = goodsDescription;
    }

    public String getMarks() {
        return marks;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public BigDecimal getGrossWeightKgs() {
        return grossWeightKgs;
    }

    public void setGrossWeightKgs(BigDecimal grossWeightKgs) {
        this.grossWeightKgs = grossWeightKgs;
    }

    public BigDecimal getMeasurementCbm() {
        return measurementCbm;
    }

    public void setMeasurementCbm(BigDecimal measurementCbm) {
        this.measurementCbm = measurementCbm;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceMode() {
        return serviceMode;
    }

    public void setServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
    }

    public String getRevenueTons() {
        return revenueTons;
    }

    public void setRevenueTons(String revenueTons) {
        this.revenueTons = revenueTons;
    }

    public String getFreightTerm() {
        return freightTerm;
    }

    public void setFreightTerm(String freightTerm) {
        this.freightTerm = freightTerm;
    }

    public String getFreightRate() {
        return freightRate;
    }

    public void setFreightRate(String freightRate) {
        this.freightRate = freightRate;
    }

    public String getPrepaidAmount() {
        return prepaidAmount;
    }

    public void setPrepaidAmount(String prepaidAmount) {
        this.prepaidAmount = prepaidAmount;
    }

    public String getCollectAmount() {
        return collectAmount;
    }

    public void setCollectAmount(String collectAmount) {
        this.collectAmount = collectAmount;
    }

    public String getPayableAt() {
        return payableAt;
    }

    public void setPayableAt(String payableAt) {
        this.payableAt = payableAt;
    }

    public String getOriginalBlCount() {
        return originalBlCount;
    }

    public void setOriginalBlCount(String originalBlCount) {
        this.originalBlCount = originalBlCount;
    }

    public String getIssuePlace() {
        return issuePlace;
    }

    public void setIssuePlace(String issuePlace) {
        this.issuePlace = issuePlace;
    }

    public String getLadenOnBoard() {
        return ladenOnBoard;
    }

    public void setLadenOnBoard(String ladenOnBoard) {
        this.ladenOnBoard = ladenOnBoard;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("blNo", getBlNo())
                .append("bookingNo", getBookingNo())
                .append("containerNo", getContainerNo())
                .append("sealNo", getSealNo())
                .append("vgmWeight", getVgmWeight())
                .toString();
    }
}

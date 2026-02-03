package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 提单信息对象 bill_of_lading
 * 
 * @author ruoyi
 * @date 2026-01-29
 */
public class BillOfLading extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** ID */
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

    /** 发货人 */
    @Excel(name = "发货人")
    private String shipper;

    /** 收货人 */
    @Excel(name = "收货人")
    private String consignee;

    /** 通知方 */
    @Excel(name = "通知方")
    private String notifyParty;

    /** 承运代理 */
    @Excel(name = "承运代理")
    private String carrierAgent;

    /** 交付申请方 */
    @Excel(name = "交付申请方")
    private String deliveryAgent;

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

    /** 集装箱与封条号 */
    @Excel(name = "集装箱与封条号")
    private String containerSealInfo;

    /** 包装件数 */
    @Excel(name = "包装件数")
    private Integer packageQuantity;

    /** 包装单位 */
    @Excel(name = "包装单位")
    private String packageUnit;

    /** 货物描述 */
    @Excel(name = "货物描述")
    private String goodsDescription;

    /** 总重(KG) */
    @Excel(name = "总重(KG)")
    private BigDecimal grossWeightKgs;

    /** 体积(CBM) */
    @Excel(name = "体积(CBM)")
    private BigDecimal measurementCbm;

    /** 运费条款 */
    @Excel(name = "运费条款")
    private String freightTerm;

    /** 正本份数 */
    @Excel(name = "正本份数")
    private String originalBlCount;

    /** 签发地点 */
    @Excel(name = "签发地点")
    private String issuePlace;

    /** 服务类型 */
    @Excel(name = "服务类型")
    private String serviceType;

    /** 服务模式 */
    @Excel(name = "服务模式")
    private String serviceMode;

    /** 计费吨 */
    @Excel(name = "计费吨")
    private BigDecimal revenueTons;

    /** 运费费率 */
    @Excel(name = "运费费率")
    private String freightRate;

    /** 预付金额 */
    @Excel(name = "预付金额")
    private String prepaidAmount;

    /** 到付金额 */
    @Excel(name = "到付金额")
    private String collectAmount;

    /** 付款地点 */
    @Excel(name = "付款地点")
    private String payableAt;

    /** 装船信息 */
    @Excel(name = "装船信息")
    private String ladenOnBoard;

    /** 唛头 */
    @Excel(name = "唛头")
    private String marks;

    /** PDF文件路径 */
    private String filePath;

    /** 集装箱号 */
    @Excel(name = "集装箱号")
    private String containerNo;

    /** 封号 */
    @Excel(name = "封号")
    private String sealNo;

    /** 箱重 */
    @Excel(name = "箱重")
    private BigDecimal containerWeight;

    /** VGM核实总重 */
    @Excel(name = "VGM核实总重")
    private BigDecimal vgmWeight;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setBlNo(String blNo) {
        this.blNo = blNo;
    }

    public String getBlNo() {
        return blNo;
    }

    public void setBookingNo(String bookingNo) {
        this.bookingNo = bookingNo;
    }

    public String getBookingNo() {
        return bookingNo;
    }

    public void setDocNo(String docNo) {
        this.docNo = docNo;
    }

    public String getDocNo() {
        return docNo;
    }

    public void setSerialNo(String serialNo) {
        this.serialNo = serialNo;
    }

    public String getSerialNo() {
        return serialNo;
    }

    public void setShipper(String shipper) {
        this.shipper = shipper;
    }

    public String getShipper() {
        return shipper;
    }

    public void setConsignee(String consignee) {
        this.consignee = consignee;
    }

    public String getConsignee() {
        return consignee;
    }

    public void setNotifyParty(String notifyParty) {
        this.notifyParty = notifyParty;
    }

    public String getNotifyParty() {
        return notifyParty;
    }

    public void setCarrierAgent(String carrierAgent) {
        this.carrierAgent = carrierAgent;
    }

    public String getCarrierAgent() {
        return carrierAgent;
    }

    public void setDeliveryAgent(String deliveryAgent) {
        this.deliveryAgent = deliveryAgent;
    }

    public String getDeliveryAgent() {
        return deliveryAgent;
    }

    public void setVesselVoyage(String vesselVoyage) {
        this.vesselVoyage = vesselVoyage;
    }

    public String getVesselVoyage() {
        return vesselVoyage;
    }

    public void setPreCarriageBy(String preCarriageBy) {
        this.preCarriageBy = preCarriageBy;
    }

    public String getPreCarriageBy() {
        return preCarriageBy;
    }

    public void setPlaceOfReceipt(String placeOfReceipt) {
        this.placeOfReceipt = placeOfReceipt;
    }

    public String getPlaceOfReceipt() {
        return placeOfReceipt;
    }

    public void setPortOfLoading(String portOfLoading) {
        this.portOfLoading = portOfLoading;
    }

    public String getPortOfLoading() {
        return portOfLoading;
    }

    public void setPortOfDischarge(String portOfDischarge) {
        this.portOfDischarge = portOfDischarge;
    }

    public String getPortOfDischarge() {
        return portOfDischarge;
    }

    public void setPlaceOfDelivery(String placeOfDelivery) {
        this.placeOfDelivery = placeOfDelivery;
    }

    public String getPlaceOfDelivery() {
        return placeOfDelivery;
    }

    public void setContainerSealInfo(String containerSealInfo) {
        this.containerSealInfo = containerSealInfo;
    }

    public String getContainerSealInfo() {
        return containerSealInfo;
    }

    public void setPackageQuantity(Integer packageQuantity) {
        this.packageQuantity = packageQuantity;
    }

    public Integer getPackageQuantity() {
        return packageQuantity;
    }

    public void setPackageUnit(String packageUnit) {
        this.packageUnit = packageUnit;
    }

    public String getPackageUnit() {
        return packageUnit;
    }

    public void setGoodsDescription(String goodsDescription) {
        this.goodsDescription = goodsDescription;
    }

    public String getGoodsDescription() {
        return goodsDescription;
    }

    public void setGrossWeightKgs(BigDecimal grossWeightKgs) {
        this.grossWeightKgs = grossWeightKgs;
    }

    public BigDecimal getGrossWeightKgs() {
        return grossWeightKgs;
    }

    public void setMeasurementCbm(BigDecimal measurementCbm) {
        this.measurementCbm = measurementCbm;
    }

    public BigDecimal getMeasurementCbm() {
        return measurementCbm;
    }

    public void setFreightTerm(String freightTerm) {
        this.freightTerm = freightTerm;
    }

    public String getFreightTerm() {
        return freightTerm;
    }

    public void setOriginalBlCount(String originalBlCount) {
        this.originalBlCount = originalBlCount;
    }

    public String getOriginalBlCount() {
        return originalBlCount;
    }

    public void setIssuePlace(String issuePlace) {
        this.issuePlace = issuePlace;
    }

    public String getIssuePlace() {
        return issuePlace;
    }

    public void setServiceType(String serviceType) {
        this.serviceType = serviceType;
    }

    public String getServiceType() {
        return serviceType;
    }

    public void setServiceMode(String serviceMode) {
        this.serviceMode = serviceMode;
    }

    public String getServiceMode() {
        return serviceMode;
    }

    public void setRevenueTons(BigDecimal revenueTons) {
        this.revenueTons = revenueTons;
    }

    public BigDecimal getRevenueTons() {
        return revenueTons;
    }

    public void setFreightRate(String freightRate) {
        this.freightRate = freightRate;
    }

    public String getFreightRate() {
        return freightRate;
    }

    public void setPrepaidAmount(String prepaidAmount) {
        this.prepaidAmount = prepaidAmount;
    }

    public String getPrepaidAmount() {
        return prepaidAmount;
    }

    public void setCollectAmount(String collectAmount) {
        this.collectAmount = collectAmount;
    }

    public String getCollectAmount() {
        return collectAmount;
    }

    public void setPayableAt(String payableAt) {
        this.payableAt = payableAt;
    }

    public String getPayableAt() {
        return payableAt;
    }

    public void setLadenOnBoard(String ladenOnBoard) {
        this.ladenOnBoard = ladenOnBoard;
    }

    public String getLadenOnBoard() {
        return ladenOnBoard;
    }

    public void setMarks(String marks) {
        this.marks = marks;
    }

    public String getMarks() {
        return marks;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setContainerNo(String containerNo) {
        this.containerNo = containerNo;
    }

    public String getContainerNo() {
        return containerNo;
    }

    public void setSealNo(String sealNo) {
        this.sealNo = sealNo;
    }

    public String getSealNo() {
        return sealNo;
    }

    public void setContainerWeight(BigDecimal containerWeight) {
        this.containerWeight = containerWeight;
    }

    public BigDecimal getContainerWeight() {
        return containerWeight;
    }

    public void setVgmWeight(BigDecimal vgmWeight) {
        this.vgmWeight = vgmWeight;
    }

    public BigDecimal getVgmWeight() {
        return vgmWeight;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("blNo", getBlNo())
                .append("bookingNo", getBookingNo())
                .append("docNo", getDocNo())
                .append("shipper", getShipper())
                .append("consignee", getConsignee())
                .append("notifyParty", getNotifyParty())
                .append("carrierAgent", getCarrierAgent())
                .append("deliveryAgent", getDeliveryAgent())
                .append("vesselVoyage", getVesselVoyage())
                .append("placeOfReceipt", getPlaceOfReceipt())
                .append("portOfLoading", getPortOfLoading())
                .append("portOfDischarge", getPortOfDischarge())
                .append("placeOfDelivery", getPlaceOfDelivery())
                .append("containerSealInfo", getContainerSealInfo())
                .append("packageQuantity", getPackageQuantity())
                .append("packageUnit", getPackageUnit())
                .append("goodsDescription", getGoodsDescription())
                .append("grossWeightKgs", getGrossWeightKgs())
                .append("measurementCbm", getMeasurementCbm())
                .append("freightTerm", getFreightTerm())
                .append("originalBlCount", getOriginalBlCount())
                .append("issuePlace", getIssuePlace())
                .append("serialNo", getSerialNo())
                .append("serviceType", getServiceType())
                .append("revenueTons", getRevenueTons())
                .append("freightRate", getFreightRate())
                .append("prepaidAmount", getPrepaidAmount())
                .append("collectAmount", getCollectAmount())
                .append("payableAt", getPayableAt())
                .append("ladenOnBoard", getLadenOnBoard())
                .append("marks", getMarks())
                .append("preCarriageBy", getPreCarriageBy())
                .append("serviceMode", getServiceMode())
                .append("containerNo", getContainerNo())
                .append("sealNo", getSealNo())
                .append("containerWeight", getContainerWeight())
                .append("vgmWeight", getVgmWeight())
                .append("filePath", getFilePath())
                .append("createTime", getCreateTime())
                .toString();
    }
}

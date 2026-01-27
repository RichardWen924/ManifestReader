package com.ruoyi.system.domain;

import java.math.BigDecimal;
import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 订舱与集装箱合并信息对象 booking_consolidated
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
public class BookingConsolidated extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 订舱编号 */
    private String bookingNo;

    /** 发货人信息 */
    @Excel(name = "发货人信息")
    private String shipper;

    /** 收货人信息 */
    @Excel(name = "收货人信息")
    private String consignee;

    /** 通知方信息 */
    @Excel(name = "通知方信息")
    private String notifyParty;

    /** 船名航次 */
    @Excel(name = "船名航次")
    private String vesselVoyage;

    /** 装货港 */
    @Excel(name = "装货港")
    private String portOfLoading;

    /** 卸货港 */
    @Excel(name = "卸货港")
    private String portOfDischarge;

    /** 交货地 */
    @Excel(name = "交货地")
    private String placeOfDelivery;

    /** 货物描述 */
    @Excel(name = "货物描述")
    private String cargoDescription;

    /** 货物数量 */
    @Excel(name = "货物数量")
    private String cargoQuantity;

    /** 货物毛重(KGS) */
    @Excel(name = "货物毛重(KGS)")
    private BigDecimal cargoGrossWeight;

    /** 货物体积(CBM) */
    @Excel(name = "货物体积(CBM)")
    private BigDecimal cargoMeasurement;

    /** 集装箱号 */
    @Excel(name = "集装箱号")
    private String containerNo;

    /** 封条号 */
    @Excel(name = "封条号")
    private String sealNo;

    /** 核验总重 */
    @Excel(name = "核验总重")
    private BigDecimal vgm;

    /** 重量单位 */
    @Excel(name = "重量单位")
    private String vgmUnit;

    /** 文件存储路径 */
    @Excel(name = "文件存储路径")
    private String filePath;

    /** 创建时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "创建时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date createdAt;

    /** 更新时间 */
    @JsonFormat(pattern = "yyyy-MM-dd", timezone = "GMT+8")
    @Excel(name = "更新时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date updatedAt;

    public void setBookingNo(String bookingNo) 
    {
        this.bookingNo = bookingNo;
    }

    public String getBookingNo() 
    {
        return bookingNo;
    }

    public void setShipper(String shipper) 
    {
        this.shipper = shipper;
    }

    public String getShipper() 
    {
        return shipper;
    }

    public void setConsignee(String consignee) 
    {
        this.consignee = consignee;
    }

    public String getConsignee() 
    {
        return consignee;
    }

    public void setNotifyParty(String notifyParty) 
    {
        this.notifyParty = notifyParty;
    }

    public String getNotifyParty() 
    {
        return notifyParty;
    }

    public void setVesselVoyage(String vesselVoyage) 
    {
        this.vesselVoyage = vesselVoyage;
    }

    public String getVesselVoyage() 
    {
        return vesselVoyage;
    }

    public void setPortOfLoading(String portOfLoading) 
    {
        this.portOfLoading = portOfLoading;
    }

    public String getPortOfLoading() 
    {
        return portOfLoading;
    }

    public void setPortOfDischarge(String portOfDischarge) 
    {
        this.portOfDischarge = portOfDischarge;
    }

    public String getPortOfDischarge() 
    {
        return portOfDischarge;
    }

    public void setPlaceOfDelivery(String placeOfDelivery) 
    {
        this.placeOfDelivery = placeOfDelivery;
    }

    public String getPlaceOfDelivery() 
    {
        return placeOfDelivery;
    }

    public void setCargoDescription(String cargoDescription) 
    {
        this.cargoDescription = cargoDescription;
    }

    public String getCargoDescription() 
    {
        return cargoDescription;
    }

    public void setCargoQuantity(String cargoQuantity) 
    {
        this.cargoQuantity = cargoQuantity;
    }

    public String getCargoQuantity() 
    {
        return cargoQuantity;
    }

    public void setCargoGrossWeight(BigDecimal cargoGrossWeight) 
    {
        this.cargoGrossWeight = cargoGrossWeight;
    }

    public BigDecimal getCargoGrossWeight() 
    {
        return cargoGrossWeight;
    }

    public void setCargoMeasurement(BigDecimal cargoMeasurement) 
    {
        this.cargoMeasurement = cargoMeasurement;
    }

    public BigDecimal getCargoMeasurement() 
    {
        return cargoMeasurement;
    }

    public void setContainerNo(String containerNo) 
    {
        this.containerNo = containerNo;
    }

    public String getContainerNo() 
    {
        return containerNo;
    }

    public void setSealNo(String sealNo) 
    {
        this.sealNo = sealNo;
    }

    public String getSealNo() 
    {
        return sealNo;
    }

    public void setVgm(BigDecimal vgm) 
    {
        this.vgm = vgm;
    }

    public BigDecimal getVgm() 
    {
        return vgm;
    }

    public void setVgmUnit(String vgmUnit) 
    {
        this.vgmUnit = vgmUnit;
    }

    public String getVgmUnit() 
    {
        return vgmUnit;
    }

    public void setFilePath(String filePath) 
    {
        this.filePath = filePath;
    }

    public String getFilePath() 
    {
        return filePath;
    }

    public void setCreatedAt(Date createdAt) 
    {
        this.createdAt = createdAt;
    }

    public Date getCreatedAt() 
    {
        return createdAt;
    }

    public void setUpdatedAt(Date updatedAt) 
    {
        this.updatedAt = updatedAt;
    }

    public Date getUpdatedAt() 
    {
        return updatedAt;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("bookingNo", getBookingNo())
            .append("shipper", getShipper())
            .append("consignee", getConsignee())
            .append("notifyParty", getNotifyParty())
            .append("vesselVoyage", getVesselVoyage())
            .append("portOfLoading", getPortOfLoading())
            .append("portOfDischarge", getPortOfDischarge())
            .append("placeOfDelivery", getPlaceOfDelivery())
            .append("cargoDescription", getCargoDescription())
            .append("cargoQuantity", getCargoQuantity())
            .append("cargoGrossWeight", getCargoGrossWeight())
            .append("cargoMeasurement", getCargoMeasurement())
            .append("containerNo", getContainerNo())
            .append("sealNo", getSealNo())
            .append("vgm", getVgm())
            .append("vgmUnit", getVgmUnit())
            .append("filePath", getFilePath())
            .append("createdAt", getCreatedAt())
            .append("updatedAt", getUpdatedAt())
            .toString();
    }
}

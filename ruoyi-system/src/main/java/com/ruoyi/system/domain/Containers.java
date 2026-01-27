package com.ruoyi.system.domain;

import java.math.BigDecimal;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 集装箱信息对象 containers
 * 
 * @author ruoyi
 * @date 2026-01-27
 */
public class Containers extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 集装箱ID */
    private Long id;

    /** 订舱编号 */
    @Excel(name = "订舱编号")
    private String bookingNo;

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

    public void setId(Long id) 
    {
        this.id = id;
    }

    public Long getId() 
    {
        return id;
    }

    public void setBookingNo(String bookingNo) 
    {
        this.bookingNo = bookingNo;
    }

    public String getBookingNo() 
    {
        return bookingNo;
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

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("bookingNo", getBookingNo())
            .append("containerNo", getContainerNo())
            .append("sealNo", getSealNo())
            .append("vgm", getVgm())
            .append("vgmUnit", getVgmUnit())
            .toString();
    }
}

package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;

/**
 * 提单费用与签发信息对象 bl_freight
 * 
 * @author Richard
 */
@TableName("bl_freight")
public class BlFreight implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 关联主表ID */
    @TableId(type = IdType.INPUT)
    private Long blId;

    /** 服务类型 */
    private String serviceType;

    /** 服务模式 */
    private String serviceMode;

    /** 计费吨 */
    private String revenueTons;

    /** 运费条款 */
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

    public Long getBlId() { return blId; }
    public void setBlId(Long blId) { this.blId = blId; }

    public String getServiceType() { return serviceType; }
    public void setServiceType(String serviceType) { this.serviceType = serviceType; }

    public String getServiceMode() { return serviceMode; }
    public void setServiceMode(String serviceMode) { this.serviceMode = serviceMode; }

    public String getRevenueTons() { return revenueTons; }
    public void setRevenueTons(String revenueTons) { this.revenueTons = revenueTons; }

    public String getFreightTerm() { return freightTerm; }
    public void setFreightTerm(String freightTerm) { this.freightTerm = freightTerm; }

    public String getFreightRate() { return freightRate; }
    public void setFreightRate(String freightRate) { this.freightRate = freightRate; }

    public String getPrepaidAmount() { return prepaidAmount; }
    public void setPrepaidAmount(String prepaidAmount) { this.prepaidAmount = prepaidAmount; }

    public String getCollectAmount() { return collectAmount; }
    public void setCollectAmount(String collectAmount) { this.collectAmount = collectAmount; }

    public String getPayableAt() { return payableAt; }
    public void setPayableAt(String payableAt) { this.payableAt = payableAt; }

    public String getOriginalBlCount() { return originalBlCount; }
    public void setOriginalBlCount(String originalBlCount) { this.originalBlCount = originalBlCount; }

    public String getIssuePlace() { return issuePlace; }
    public void setIssuePlace(String issuePlace) { this.issuePlace = issuePlace; }

    public String getLadenOnBoard() { return ladenOnBoard; }
    public void setLadenOnBoard(String ladenOnBoard) { this.ladenOnBoard = ladenOnBoard; }

    @Override
    public String toString() {
        return "BlFreight{" +
                "blId=" + blId +
                ", serviceType='" + serviceType + '\'' +
                ", freightTerm='" + freightTerm + '\'' +
                '}';
    }
}

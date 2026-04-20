package com.ruoyi.system.domain;

import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.IdType;
import java.io.Serializable;

/**
 * 提单参与方对象 bl_parties
 * 
 * @author Richard
 */
@TableName("bl_parties")
public class BlParties implements Serializable {
    private static final long serialVersionUID = 1L;

    /** 关联主表ID */
    @TableId(type = IdType.INPUT)
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

    public Long getBlId() { return blId; }
    public void setBlId(Long blId) { this.blId = blId; }

    public String getShipper() { return shipper; }
    public void setShipper(String shipper) { this.shipper = shipper; }

    public String getConsignee() { return consignee; }
    public void setConsignee(String consignee) { this.consignee = consignee; }

    public String getNotifyParty() { return notifyParty; }
    public void setNotifyParty(String notifyParty) { this.notifyParty = notifyParty; }

    public String getCarrierAgent() { return carrierAgent; }
    public void setCarrierAgent(String carrierAgent) { this.carrierAgent = carrierAgent; }

    public String getDeliveryAgent() { return deliveryAgent; }
    public void setDeliveryAgent(String deliveryAgent) { this.deliveryAgent = deliveryAgent; }

    @Override
    public String toString() {
        return "BlParties{" +
                "blId=" + blId +
                ", shipper='" + shipper + '\'' +
                ", consignee='" + consignee + '\'' +
                ", notifyParty='" + notifyParty + '\'' +
                ", carrierAgent='" + carrierAgent + '\'' +
                ", deliveryAgent='" + deliveryAgent + '\'' +
                '}';
    }
}

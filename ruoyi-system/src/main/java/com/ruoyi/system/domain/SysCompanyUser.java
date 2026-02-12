package com.ruoyi.system.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.ruoyi.common.annotation.Excel;
import com.ruoyi.common.core.domain.BaseEntity;

/**
 * 公司用户对象 sys_company_user
 * 
 * @author Richard
 * @date 2026-02-05
 */
public class SysCompanyUser extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long userId;

    /** 公司名 */
    @Excel(name = "公司名")
    private String companyName;

    /** 公司编号 (对应业务表 create_by) */
    @Excel(name = "公司编号 (对应业务表 create_by)")
    private String companyCode;

    /** 公司四字缩写 (对应docNo前四位) */
    @Excel(name = "公司四字缩写 (对应docNo前四位)")
    private String companyAbbr;

    /** 密码 (建议加密存储) */
    @Excel(name = "密码 (建议加密存储)")
    private String password;

    /** 属于该公司的业务数据条数 */
    @Excel(name = "属于该公司的业务数据条数")
    private Long dataCount;

    /** 帐号状态（0正常 1停用） */
    @Excel(name = "帐号状态", readConverterExp = "0=正常,1=停用")
    private String status;

    /** 会员状态（0普通, 1会员） */
    @Excel(name = "会员状态", readConverterExp = "0=普通,1=会员")
    private String vipStatus;

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyCode(String companyCode) {
        this.companyCode = companyCode;
    }

    public String getCompanyCode() {
        return companyCode;
    }

    public void setCompanyAbbr(String companyAbbr) {
        this.companyAbbr = companyAbbr;
    }

    public String getCompanyAbbr() {
        return companyAbbr;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPassword() {
        return password;
    }

    public void setDataCount(Long dataCount) {
        this.dataCount = dataCount;
    }

    public Long getDataCount() {
        return dataCount;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setVipStatus(String vipStatus) {
        this.vipStatus = vipStatus;
    }

    public String getVipStatus() {
        return vipStatus;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("userId", getUserId())
                .append("companyName", getCompanyName())
                .append("companyCode", getCompanyCode())
                .append("companyAbbr", getCompanyAbbr())
                .append("password", getPassword())
                .append("dataCount", getDataCount())
                .append("status", getStatus())
                .append("vipStatus", getVipStatus())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}

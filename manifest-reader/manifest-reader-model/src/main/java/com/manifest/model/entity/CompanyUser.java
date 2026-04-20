package com.manifest.model.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.manifest.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

/**
 * 公司用户表 bl_company_user
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("bl_company_user")
public class CompanyUser extends BaseEntity {

    @TableId(type = IdType.AUTO)
    private Long userId;

    /** 公司名 */
    private String companyName;

    /** 公司编号（作为登录账号） */
    private String companyCode;

    /** 公司四字缩写（提单号前缀） */
    private String companyAbbr;

    /** 密码（BCrypt加密） */
    private String password;

    /** 帐号状态 0正常 1停用 */
    private String status;

    /** 会员状态 0普通 1会员 */
    private String vipStatus;

    /** 会员到期时间 */
    private Date expiryDate;

    /** 套餐类型 */
    private String packageType;

    /** 已使用数据条数 */
    private Long dataCount;
}

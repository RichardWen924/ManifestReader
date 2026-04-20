package com.manifest.model.dto;

import lombok.Data;

/**
 * 登录请求 DTO
 */
@Data
public class LoginDto {
    /** 公司编号（账号） */
    private String companyCode;
    /** 密码 */
    private String password;
}

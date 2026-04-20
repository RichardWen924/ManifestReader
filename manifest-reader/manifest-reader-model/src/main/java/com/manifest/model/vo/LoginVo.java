package com.manifest.model.vo;

import lombok.Data;

/**
 * 登录响应 VO
 */
@Data
public class LoginVo {
    private Long userId;
    private String companyCode;
    private String companyName;
    private String companyAbbr;
    private String token;
}

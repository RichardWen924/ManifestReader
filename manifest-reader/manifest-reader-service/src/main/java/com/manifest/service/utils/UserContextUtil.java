package com.manifest.service.utils;

import cn.hutool.core.util.StrUtil;
import com.manifest.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;

/**
 * 从网关传入的 Header 中获取当前登录用户信息
 * 网关已完成 JWT 解析并注入 user-id / company-code Header
 */
public class UserContextUtil {

    public static String getUserId() {
        return getHeader("user-id");
    }

    public static String getCompanyCode() {
        return getHeader("company-code");
    }

    private static String getHeader(String key) {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;
        return attrs.getRequest().getHeader(key);
    }
}

package com.manifest.auth.service;

import cn.hutool.crypto.digest.BCrypt;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.manifest.auth.mapper.CompanyUserMapper;
import com.manifest.common.exception.ServiceException;
import com.manifest.common.utils.JwtUtil;
import com.manifest.model.dto.LoginDto;
import com.manifest.model.entity.CompanyUser;
import com.manifest.model.vo.LoginVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

/**
 * 认证 Service — 继承 ServiceImpl，使用 MP lambdaQuery 替代手写 QueryWrapper
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService extends ServiceImpl<CompanyUserMapper, CompanyUser> {

    private final RedisTemplate<String, Object> redisTemplate;

    public LoginVo login(LoginDto dto) {
        // MP lambdaQuery 链式查询，替代 new LambdaQueryWrapper<>()
        CompanyUser user = lambdaQuery()
                .eq(CompanyUser::getCompanyCode, dto.getCompanyCode())
                .eq(CompanyUser::getStatus, "0")
                .one();

        if (user == null) throw new ServiceException(401, "账号不存在或已停用");
        if (!BCrypt.checkpw(dto.getPassword(), user.getPassword())) throw new ServiceException(401, "密码错误");

        String token = JwtUtil.createToken(String.valueOf(user.getUserId()), user.getCompanyCode());

        // 用户信息缓存至 Redis（7天）
        redisTemplate.opsForValue().set("login:user:" + user.getUserId(), user, 7, TimeUnit.DAYS);

        log.info("[Auth] 登录成功: companyCode={}", user.getCompanyCode());

        LoginVo vo = new LoginVo();
        vo.setUserId(user.getUserId());
        vo.setCompanyCode(user.getCompanyCode());
        vo.setCompanyName(user.getCompanyName());
        vo.setCompanyAbbr(user.getCompanyAbbr());
        vo.setToken(token);
        return vo;
    }

    public void logout(String userId) {
        redisTemplate.delete("login:user:" + userId);
        log.info("[Auth] 退出: userId={}", userId);
    }
}

package com.manifest.auth.controller;

import com.manifest.auth.service.AuthService;
import com.manifest.common.response.R;
import com.manifest.model.dto.LoginDto;
import com.manifest.model.vo.LoginVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    /**
     * 登录接口
     * POST /auth/login
     */
    @PostMapping("/login")
    public R<LoginVo> login(@RequestBody LoginDto dto) {
        return R.ok(authService.login(dto));
    }

    /**
     * 退出登录
     * POST /auth/logout
     */
    @PostMapping("/logout")
    public R<Void> logout(@RequestHeader("user-id") String userId) {
        authService.logout(userId);
        return R.ok();
    }
}

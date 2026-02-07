package com.soundspirit.controller;

import com.soundspirit.common.Result;
import com.soundspirit.dto.LoginRequest;
import com.soundspirit.dto.LoginResponse;
import com.soundspirit.entity.User;
import com.soundspirit.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    /**
     * 游客登录
     */
    @PostMapping("/guest")
    public Result<LoginResponse> guestLogin(@RequestBody LoginRequest request) {
        String token = userService.guestLogin(request.getDeviceId());
        return Result.success(new LoginResponse(token));
    }

    /**
     * 微信登录
     */
    @PostMapping("/wechat")
    public Result<LoginResponse> wechatLogin(@RequestBody LoginRequest request) {
        String token = userService.wechatLogin(request.getCode());
        return Result.success(new LoginResponse(token));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestAttribute("userId") Long userId) {
        User user = userService.getUserById(userId);
        return Result.success(user);
    }
}

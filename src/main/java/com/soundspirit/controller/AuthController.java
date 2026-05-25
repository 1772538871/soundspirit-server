package com.soundspirit.controller;

import com.soundspirit.common.BusinessException;
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
        if (request.getDeviceId() == null || request.getDeviceId().isBlank()) {
            throw new BusinessException(400, "deviceId不能为空");
        }
        String token = userService.guestLogin(request.getDeviceId());
        return Result.success(new LoginResponse(token));
    }

    /**
     * 微信登录
     */
    @PostMapping("/wechat")
    public Result<LoginResponse> wechatLogin(@RequestBody LoginRequest request) {
        if (request.getCode() == null || request.getCode().isBlank()) {
            throw new BusinessException(400, "code不能为空");
        }
        String token = userService.wechatLogin(request.getCode());
        return Result.success(new LoginResponse(token));
    }

    /**
     * 获取当前用户信息
     */
    @GetMapping("/me")
    public Result<User> getCurrentUser(@RequestAttribute("userId") Long userId) {
        User user = userService.getUserById(userId);
        if (user == null) {
            throw new BusinessException(404, "用户不存在");
        }
        return Result.success(user);
    }
}

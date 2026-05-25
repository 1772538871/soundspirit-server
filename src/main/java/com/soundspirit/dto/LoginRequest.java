package com.soundspirit.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    /**
     * 设备ID（游客登录）
     */
    @Size(max = 128, message = "设备ID长度不能超过128")
    private String deviceId;

    /**
     * 微信授权码（微信登录）
     */
    @Size(max = 256, message = "授权码长度不能超过256")
    private String code;
}

package com.soundspirit.dto;

import lombok.Data;

/**
 * 登录请求DTO
 */
@Data
public class LoginRequest {
    /**
     * 设备ID（游客登录）
     */
    private String deviceId;

    /**
     * 微信授权码（微信登录）
     */
    private String code;
}

package com.soundspirit.dto;

import lombok.Data;

/**
 * WebSocket消息DTO
 */
@Data
public class WebSocketMessage {

    /**
     * 消息类型
     */
    private String type;

    /**
     * 会话ID
     */
    private Long sessionId;

    /**
     * 角色ID
     */
    private Long characterId;

    /**
     * 文本内容
     */
    private String text;

    /**
     * 音频数据（Base64编码）
     */
    private String audioData;

    /**
     * 是否最终结果
     */
    private Boolean isFinal;

    /**
     * 序列号
     */
    private Integer sequence;

    // 消息类型常量
    public static final String TYPE_AUDIO_START = "AUDIO_START";
    public static final String TYPE_AUDIO_DATA = "AUDIO_DATA";
    public static final String TYPE_AUDIO_END = "AUDIO_END";
    public static final String TYPE_ASR_RESULT = "ASR_RESULT";
    public static final String TYPE_AI_TEXT = "AI_TEXT";
    public static final String TYPE_AI_TEXT_END = "AI_TEXT_END";
    public static final String TYPE_AI_AUDIO = "AI_AUDIO";
    public static final String TYPE_AI_AUDIO_END = "AI_AUDIO_END";
    public static final String TYPE_ERROR = "ERROR";
    public static final String TYPE_HEARTBEAT = "HEARTBEAT";
}

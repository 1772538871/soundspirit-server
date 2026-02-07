package com.soundspirit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * AI角色实体
 */
@Data
@TableName("ai_character")
public class AiCharacter {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String name;

    private String englishName;

    /**
     * 性别: 1-男 2-女 3-未知
     */
    private Integer gender;

    private Integer age;

    private String personality;

    private String description;

    /**
     * 系统提示词
     */
    private String systemPrompt;

    /**
     * 开场白
     */
    private String greeting;

    /**
     * 头像URL
     */
    private String avatar;

    /**
     * 背景图URL
     */
    private String backgroundImage;

    /**
     * TTS音色
     */
    private String voiceType;

    private String category;

    private String tags;

    private Integer sortOrder;

    /**
     * 状态: 0-禁用 1-正常
     */
    private Integer status;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createTime;

    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}

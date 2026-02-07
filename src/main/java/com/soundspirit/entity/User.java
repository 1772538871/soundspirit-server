package com.soundspirit.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 用户实体
 */
@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String openId;

    private String unionId;

    private String nickname;

    private String avatar;

    private String phone;

    /**
     * 用户类型: 1-游客 2-微信用户 3-手机用户
     */
    private Integer userType;

    /**
     * 会员等级: 0-普通 1-月卡 2-年卡
     */
    private Integer vipLevel;

    private LocalDateTime vipExpireTime;

    /**
     * 剩余对话次数（游客模式）
     */
    private Integer remainingChats;

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

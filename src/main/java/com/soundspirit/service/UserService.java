package com.soundspirit.service;

import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soundspirit.common.BusinessException;
import com.soundspirit.entity.User;
import com.soundspirit.repository.UserMapper;
import com.soundspirit.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 用户服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;

    private static final int GUEST_FREE_CHATS = 5;

    /**
     * 游客登录
     */
    public String guestLogin(String deviceId) {
        // 查找或创建游客用户
        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getOpenId, "guest_" + deviceId)
                        .eq(User::getUserType, 1)
        );

        if (user == null) {
            user = new User();
            user.setOpenId("guest_" + deviceId);
            user.setNickname("游客" + IdUtil.fastSimpleUUID().substring(0, 6));
            user.setUserType(1);
            user.setVipLevel(0);
            user.setRemainingChats(GUEST_FREE_CHATS);
            user.setStatus(1);
            userMapper.insert(user);
            log.info("创建游客用户: {}", user.getId());
        }

        return jwtUtil.generateToken(user.getId());
    }

    /**
     * 微信登录
     */
    public String wechatLogin(String code) {
        // TODO: 调用微信API获取openId
        // 这里是简化实现
        String openId = "wx_" + code;

        User user = userMapper.selectOne(
                new LambdaQueryWrapper<User>()
                        .eq(User::getOpenId, openId)
                        .eq(User::getUserType, 2)
        );

        if (user == null) {
            user = new User();
            user.setOpenId(openId);
            user.setNickname("微信用户");
            user.setUserType(2);
            user.setVipLevel(0);
            user.setRemainingChats(-1); // 微信用户无限制
            user.setStatus(1);
            userMapper.insert(user);
            log.info("创建微信用户: {}", user.getId());
        }

        return jwtUtil.generateToken(user.getId());
    }

    /**
     * 获取用户信息
     */
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 检查并扣减对话次数（游客模式）
     */
    public boolean checkAndDeductChat(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }

        // VIP用户或微信用户无限制
        if (user.getVipLevel() > 0 || user.getUserType() == 2) {
            return true;
        }

        // 游客检查剩余次数
        if (user.getRemainingChats() <= 0) {
            return false;
        }

        user.setRemainingChats(user.getRemainingChats() - 1);
        userMapper.updateById(user);
        return true;
    }
}

package com.soundspirit.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.soundspirit.common.BusinessException;
import com.soundspirit.entity.ChatMessage;
import com.soundspirit.entity.ChatSession;
import com.soundspirit.repository.ChatMessageMapper;
import com.soundspirit.repository.ChatSessionMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 聊天服务
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatSessionMapper sessionMapper;
    private final ChatMessageMapper messageMapper;

    /**
     * 创建新会话
     */
    @Transactional
    public ChatSession createSession(Long userId, Long characterId) {
        ChatSession session = new ChatSession();
        session.setUserId(userId);
        session.setCharacterId(characterId);
        session.setTitle("新对话");
        session.setMessageCount(0);
        session.setStatus(1);
        sessionMapper.insert(session);
        return session;
    }

    /**
     * 获取用户的会话列表
     */
    public List<ChatSession> listUserSessions(Long userId) {
        return sessionMapper.selectList(
                new LambdaQueryWrapper<ChatSession>()
                        .eq(ChatSession::getUserId, userId)
                        .eq(ChatSession::getStatus, 1)
                        .orderByDesc(ChatSession::getLastMessageTime)
        );
    }

    /**
     * 获取会话的消息历史
     */
    public List<ChatMessage> getSessionMessages(Long sessionId, int limit) {
        return messageMapper.selectList(
                new LambdaQueryWrapper<ChatMessage>()
                        .eq(ChatMessage::getSessionId, sessionId)
                        .orderByDesc(ChatMessage::getCreateTime)
                        .last("LIMIT " + limit)
        );
    }

    /**
     * 保存消息
     */
    @Transactional
    public void saveMessage(Long sessionId, String userText, String assistantText) {
        ChatMessage userMsg = new ChatMessage();
        userMsg.setSessionId(sessionId);
        userMsg.setRole("user");
        userMsg.setContent(userText);
        messageMapper.insert(userMsg);

        ChatMessage assistantMsg = new ChatMessage();
        assistantMsg.setSessionId(sessionId);
        assistantMsg.setRole("assistant");
        assistantMsg.setContent(assistantText);
        messageMapper.insert(assistantMsg);

        ChatSession session = sessionMapper.selectById(sessionId);
        if (session != null) {
            session.setMessageCount(session.getMessageCount() + 2);
            session.setLastMessageTime(LocalDateTime.now());

            if (session.getMessageCount() == 2) {
                String title = userText.length() > 20 ? userText.substring(0, 20) + "..." : userText;
                session.setTitle(title);
            }
            sessionMapper.updateById(session);
        }
    }

    /**
     * 删除会话（带归属校验）
     */
    public void deleteSession(Long userId, Long sessionId) {
        ChatSession session = sessionMapper.selectById(sessionId);
        if (session == null) {
            throw new BusinessException(404, "会话不存在");
        }
        if (!session.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此会话");
        }
        session.setStatus(0);
        sessionMapper.updateById(session);
    }
}

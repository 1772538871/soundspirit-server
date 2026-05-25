package com.soundspirit.controller;

import com.soundspirit.common.BusinessException;
import com.soundspirit.common.Result;
import com.soundspirit.entity.AiCharacter;
import com.soundspirit.entity.ChatMessage;
import com.soundspirit.entity.ChatSession;
import com.soundspirit.service.CharacterService;
import com.soundspirit.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 聊天控制器
 */
@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final CharacterService characterService;

    /**
     * 创建新会话
     */
    @PostMapping("/sessions")
    public Result<ChatSession> createSession(
            @RequestAttribute("userId") Long userId,
            @RequestParam Long characterId) {
        AiCharacter character = characterService.getById(characterId);
        if (character == null) {
            throw new BusinessException(400, "角色不存在");
        }
        return Result.success(chatService.createSession(userId, characterId));
    }

    /**
     * 获取用户的会话列表
     */
    @GetMapping("/sessions")
    public Result<List<ChatSession>> listSessions(@RequestAttribute("userId") Long userId) {
        return Result.success(chatService.listUserSessions(userId));
    }

    /**
     * 获取会话消息历史
     */
    @GetMapping("/sessions/{sessionId}/messages")
    public Result<List<ChatMessage>> getMessages(
            @PathVariable Long sessionId,
            @RequestParam(defaultValue = "50") int limit) {
        if (limit <= 0 || limit > 200) {
            throw new BusinessException(400, "limit必须在1到200之间");
        }
        return Result.success(chatService.getSessionMessages(sessionId, limit));
    }

    /**
     * 删除会话
     */
    @DeleteMapping("/sessions/{sessionId}")
    public Result<Void> deleteSession(
            @RequestAttribute("userId") Long userId,
            @PathVariable Long sessionId) {
        chatService.deleteSession(userId, sessionId);
        return Result.success();
    }
}

package com.soundspirit.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.soundspirit.dto.WebSocketMessage;
import com.soundspirit.entity.AiCharacter;
import com.soundspirit.model.provider.LLMProvider;
import com.soundspirit.model.provider.ModelProviderFactory;
import com.soundspirit.service.CharacterService;
import com.soundspirit.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 语音聊天WebSocket处理器
 * 核心功能：ASR -> LLM -> TTS 流式处理
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VoiceChatWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ModelProviderFactory providerFactory;
    private final CharacterService characterService;
    private final ChatService chatService;

    // 会话状态管理
    private final Map<String, SessionContext> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, new SessionContext());
        log.info("WebSocket连接建立: {}", sessionId);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        try {
            WebSocketMessage msg = objectMapper.readValue(message.getPayload(), WebSocketMessage.class);
            SessionContext context = sessions.get(session.getId());

            switch (msg.getType()) {
                case WebSocketMessage.TYPE_AUDIO_START -> handleAudioStart(session, msg, context);
                case WebSocketMessage.TYPE_AUDIO_DATA -> handleAudioData(session, msg, context);
                case WebSocketMessage.TYPE_AUDIO_END -> handleAudioEnd(session, msg, context);
                case WebSocketMessage.TYPE_HEARTBEAT -> handleHeartbeat(session);
                default -> log.warn("未知消息类型: {}", msg.getType());
            }
        } catch (Exception e) {
            log.error("处理WebSocket消息失败", e);
            sendError(session, "消息处理失败: " + e.getMessage());
        }
    }

    /**
     * 处理音频开始
     */
    private void handleAudioStart(WebSocketSession session, WebSocketMessage msg, SessionContext context) {
        context.setCharacterId(msg.getCharacterId());
        context.setSessionId(msg.getSessionId());
        context.clearAudioBuffer();
        log.debug("开始接收音频, characterId: {}", msg.getCharacterId());
    }

    /**
     * 处理音频数据
     */
    private void handleAudioData(WebSocketSession session, WebSocketMessage msg, SessionContext context) {
        if (msg.getAudioData() != null) {
            context.appendAudioData(msg.getAudioData());
        }
    }

    /**
     * 处理音频结束 - 触发ASR->LLM->TTS流程
     */
    private void handleAudioEnd(WebSocketSession session, WebSocketMessage msg, SessionContext context) {
        log.debug("音频接收完成，开始处理");

        // 模拟ASR结果（实际项目中对接火山引擎ASR）
        String inputText = msg.getText();
        if (inputText == null || inputText.isEmpty()) {
            inputText = "你好"; // 测试用
        }
        final String userText = inputText;

        // 发送ASR结果
        sendAsrResult(session, userText);

        // 获取角色信息
        AiCharacter character = characterService.getById(context.getCharacterId());
        if (character == null) {
            sendError(session, "角色不存在");
            return;
        }

        // 添加到对话历史
        context.addMessage(LLMProvider.Message.user(userText));

        // 调用LLM进行流式对话
        LLMProvider llm = providerFactory.getDefaultLLMProvider();
        StringBuilder fullResponse = new StringBuilder();

        llm.chatStream(
                character.getSystemPrompt(),
                context.getMessages(),
                // 流式文本回调 - 实现LLM分段回复
                text -> {
                    fullResponse.append(text);
                    sendAiText(session, text, false);

                    // 智能分句：遇到句号等标点时触发TTS
                    if (shouldTriggerTTS(text)) {
                        String sentence = extractCompleteSentence(fullResponse.toString());
                        if (sentence != null) {
                            triggerTTS(session, sentence, context);
                        }
                    }
                },
                // 完成回调
                () -> {
                    sendAiTextEnd(session);
                    context.addMessage(LLMProvider.Message.assistant(fullResponse.toString()));

                    // 保存消息到数据库
                    chatService.saveMessage(context.getSessionId(), userText, fullResponse.toString());
                }
        );
    }

    /**
     * 判断是否应该触发TTS（遇到完整句子）
     */
    private boolean shouldTriggerTTS(String text) {
        return text.contains("。") || text.contains("！") || text.contains("？")
                || text.contains("，") || text.contains("~");
    }

    /**
     * 提取完整句子用于TTS
     */
    private String extractCompleteSentence(String buffer) {
        // 简单实现，实际需要更复杂的分句逻辑
        int lastIndex = Math.max(
                buffer.lastIndexOf("。"),
                Math.max(buffer.lastIndexOf("！"), buffer.lastIndexOf("？"))
        );
        if (lastIndex > 0) {
            return buffer.substring(0, lastIndex + 1);
        }
        return null;
    }

    /**
     * 触发TTS合成
     */
    private void triggerTTS(WebSocketSession session, String text, SessionContext context) {
        // TODO: 对接火山引擎TTS，流式返回音频
        log.debug("触发TTS: {}", text);
    }

    private void handleHeartbeat(WebSocketSession session) {
        sendMessage(session, WebSocketMessage.TYPE_HEARTBEAT, null);
    }

    private void sendAsrResult(WebSocketSession session, String text) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setType(WebSocketMessage.TYPE_ASR_RESULT);
        msg.setText(text);
        msg.setIsFinal(true);
        sendMessage(session, msg);
    }

    private void sendAiText(WebSocketSession session, String text, boolean isFinal) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setType(WebSocketMessage.TYPE_AI_TEXT);
        msg.setText(text);
        msg.setIsFinal(isFinal);
        sendMessage(session, msg);
    }

    private void sendAiTextEnd(WebSocketSession session) {
        sendMessage(session, WebSocketMessage.TYPE_AI_TEXT_END, null);
    }

    private void sendError(WebSocketSession session, String error) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setType(WebSocketMessage.TYPE_ERROR);
        msg.setText(error);
        sendMessage(session, msg);
    }

    private void sendMessage(WebSocketSession session, String type, String text) {
        WebSocketMessage msg = new WebSocketMessage();
        msg.setType(type);
        msg.setText(text);
        sendMessage(session, msg);
    }

    private void sendMessage(WebSocketSession session, WebSocketMessage msg) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(objectMapper.writeValueAsString(msg)));
            }
        } catch (Exception e) {
            log.error("发送WebSocket消息失败", e);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session.getId());
        log.info("WebSocket连接关闭: {}, status: {}", session.getId(), status);
    }

    /**
     * 会话上下文
     */
    private static class SessionContext {
        private Long characterId;
        private Long sessionId;
        private final List<LLMProvider.Message> messages = new ArrayList<>();
        private final StringBuilder audioBuffer = new StringBuilder();

        public void setCharacterId(Long characterId) {
            this.characterId = characterId;
        }

        public Long getCharacterId() {
            return characterId;
        }

        public void setSessionId(Long sessionId) {
            this.sessionId = sessionId;
        }

        public Long getSessionId() {
            return sessionId;
        }

        public void addMessage(LLMProvider.Message message) {
            messages.add(message);
            // 保持最近20条消息
            if (messages.size() > 20) {
                messages.remove(0);
            }
        }

        public List<LLMProvider.Message> getMessages() {
            return new ArrayList<>(messages);
        }

        public void appendAudioData(String data) {
            audioBuffer.append(data);
        }

        public String getAudioData() {
            return audioBuffer.toString();
        }

        public void clearAudioBuffer() {
            audioBuffer.setLength(0);
        }
    }
}

package com.soundspirit.model.provider;

import java.util.List;
import java.util.function.Consumer;

/**
 * LLM模型提供者接口 - 借鉴MaxKB多模型适配设计
 * 统一抽象不同LLM供应商的调用方式
 */
public interface LLMProvider {

    /**
     * 获取提供商名称
     */
    String getName();

    /**
     * 同步对话
     */
    String chat(String systemPrompt, List<Message> messages);

    /**
     * 流式对话 - 核心能力，用于实时语音场景
     */
    void chatStream(String systemPrompt, List<Message> messages, Consumer<String> onMessage, Runnable onComplete);

    /**
     * 消息结构
     */
    record Message(String role, String content) {
        public static Message user(String content) {
            return new Message("user", content);
        }

        public static Message assistant(String content) {
            return new Message("assistant", content);
        }
    }
}

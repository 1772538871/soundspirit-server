package com.soundspirit.model.provider;

import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import okhttp3.sse.EventSource;
import okhttp3.sse.EventSourceListener;
import okhttp3.sse.EventSources;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

/**
 * 火山引擎 LLM 提供者实现（豆包大模型）
 */
@Slf4j
@Component
public class VolcengineLLMProvider implements LLMProvider {

    private static final String API_URL = "https://ark.cn-beijing.volces.com/api/v3/chat/completions";

    @Value("${ai.volcengine.access-token:}")
    private String accessToken;

    @Value("${ai.volcengine.llm.endpoint-id:}")
    private String endpointId;

    private final OkHttpClient client;
    private final ObjectMapper objectMapper;

    public VolcengineLLMProvider() {
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    public String getName() {
        return "volcengine";
    }

    @Override
    public String chat(String systemPrompt, List<Message> messages) {
        StringBuilder result = new StringBuilder();
        chatStream(systemPrompt, messages, result::append, () -> {});
        return result.toString();
    }

    @Override
    public void chatStream(String systemPrompt, List<Message> messages,
                          Consumer<String> onMessage, Runnable onComplete) {
        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", endpointId);
            requestBody.put("stream", true);

            ArrayNode messagesArray = requestBody.putArray("messages");

            // 添加系统提示词
            if (systemPrompt != null && !systemPrompt.isEmpty()) {
                ObjectNode systemMsg = messagesArray.addObject();
                systemMsg.put("role", "system");
                systemMsg.put("content", systemPrompt);
            }

            // 添加对话历史
            for (Message msg : messages) {
                ObjectNode msgNode = messagesArray.addObject();
                msgNode.put("role", msg.role());
                msgNode.put("content", msg.content());
            }

            Request request = new Request.Builder()
                    .url(API_URL)
                    .addHeader("Authorization", "Bearer " + accessToken)
                    .addHeader("Content-Type", "application/json")
                    .post(RequestBody.create(
                            objectMapper.writeValueAsString(requestBody),
                            MediaType.parse("application/json")))
                    .build();

            EventSourceListener listener = new EventSourceListener() {
                @Override
                public void onEvent(EventSource eventSource, String id, String type, String data) {
                    if ("[DONE]".equals(data)) {
                        onComplete.run();
                        return;
                    }
                    try {
                        JsonNode json = objectMapper.readTree(data);
                        JsonNode choices = json.get("choices");
                        if (choices != null && choices.isArray() && !choices.isEmpty()) {
                            JsonNode delta = choices.get(0).get("delta");
                            if (delta != null && delta.has("content")) {
                                String content = delta.get("content").asText();
                                if (content != null && !content.isEmpty()) {
                                    onMessage.accept(content);
                                }
                            }
                        }
                    } catch (Exception e) {
                        log.error("解析SSE数据失败: {}", data, e);
                    }
                }

                @Override
                public void onFailure(EventSource eventSource, Throwable t, Response response) {
                    log.error("SSE连接失败", t);
                    onComplete.run();
                }
            };

            EventSources.createFactory(client).newEventSource(request, listener);

        } catch (Exception e) {
            log.error("调用火山引擎LLM失败", e);
            onComplete.run();
        }
    }
}

package com.soundspirit.model.provider;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 模型提供者工厂 - 借鉴MaxKB的多模型适配设计
 * 统一管理所有AI模型提供者，支持动态切换
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ModelProviderFactory {

    private final List<LLMProvider> llmProviders;
    private final Map<String, LLMProvider> llmProviderMap = new HashMap<>();

    @Value("${ai.default-provider:volcengine}")
    private String defaultProvider;

    @PostConstruct
    public void init() {
        // 注册所有LLM提供者
        for (LLMProvider provider : llmProviders) {
            llmProviderMap.put(provider.getName(), provider);
            log.info("注册LLM提供者: {}", provider.getName());
        }
        log.info("默认LLM提供者: {}", defaultProvider);
    }

    /**
     * 获取默认LLM提供者
     */
    public LLMProvider getDefaultLLMProvider() {
        return getLLMProvider(defaultProvider);
    }

    /**
     * 根据名称获取LLM提供者
     */
    public LLMProvider getLLMProvider(String name) {
        LLMProvider provider = llmProviderMap.get(name);
        if (provider == null) {
            log.warn("未找到LLM提供者: {}，使用默认提供者", name);
            provider = llmProviderMap.get(defaultProvider);
        }
        return provider;
    }

    /**
     * 获取所有可用的LLM提供者名称
     */
    public List<String> getAvailableLLMProviders() {
        return llmProviderMap.keySet().stream().toList();
    }
}

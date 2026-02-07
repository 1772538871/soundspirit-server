package com.soundspirit.model.provider;

import java.util.function.Consumer;

/**
 * ASR语音识别提供者接口
 */
public interface ASRProvider {

    /**
     * 获取提供商名称
     */
    String getName();

    /**
     * 流式语音识别 - 实时返回识别结果
     * @param audioData PCM音频数据
     * @param onResult 识别结果回调
     * @param onComplete 识别完成回调
     */
    void recognizeStream(byte[] audioData, Consumer<ASRResult> onResult, Runnable onComplete);

    /**
     * ASR识别结果
     */
    record ASRResult(String text, boolean isFinal) {}
}

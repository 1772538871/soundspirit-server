package com.soundspirit.model.provider;

import java.util.function.Consumer;

/**
 * TTS语音合成提供者接口
 */
public interface TTSProvider {

    /**
     * 获取提供商名称
     */
    String getName();

    /**
     * 流式语音合成 - 边生成边返回音频数据
     * @param text 待合成文本
     * @param voiceType 音色类型
     * @param onAudio 音频数据回调
     * @param onComplete 合成完成回调
     */
    void synthesizeStream(String text, String voiceType, Consumer<byte[]> onAudio, Runnable onComplete);

    /**
     * 同步语音合成
     */
    byte[] synthesize(String text, String voiceType);
}

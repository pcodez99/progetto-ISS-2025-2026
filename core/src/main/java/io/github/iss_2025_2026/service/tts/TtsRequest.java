package io.github.iss_2025_2026.service.tts;

import io.github.iss_2025_2026.model.NpcVoiceConfig;

public class TtsRequest {
    private final String text;
    private final String voice;
    private final float speed;

    public TtsRequest(String text, String voice, float speed) {
        this.text = text;
        this.voice = voice;
        this.speed = speed;
    }

    public static TtsRequest of(String text, TtsConfig config) {
        return of(text, config, null);
    }

    public static TtsRequest of(String text, TtsConfig config, NpcVoiceConfig npcVoice) {
        if (config.getProvider() == TtsProvider.MISTRAL) {
            String voiceId = firstNonBlank(
                    npcVoice != null ? npcVoice.getMistralVoiceId() : null,
                    config.getMistral().getVoiceId());
            return new TtsRequest(text, voiceId, 1f);
        }
        String localVoice = firstNonBlank(
                npcVoice != null ? npcVoice.getLocalVoice() : null,
                config.getLocal().getVoice());
        return new TtsRequest(text, localVoice, config.getLocal().getSpeed());
    }

    private static String firstNonBlank(String preferred, String fallback) {
        return preferred == null || preferred.trim().isEmpty() ? fallback : preferred.trim();
    }

    public String getText() {
        return text;
    }

    public String getVoice() {
        return voice;
    }

    public float getSpeed() {
        return speed;
    }
}

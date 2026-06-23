package io.github.iss_2025_2026.service.tts;

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
        if (config.getProvider() == TtsProvider.MISTRAL) {
            return new TtsRequest(text, config.getMistral().getVoiceId(), 1f);
        }
        return new TtsRequest(text, config.getLocal().getVoice(), config.getLocal().getSpeed());
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

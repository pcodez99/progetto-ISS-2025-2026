package io.github.iss_2025_2026.service.tts;

public class LocalTtsConfig {
    private final String baseUrl;
    private final String speechEndpoint;
    private final String model;
    private final String voice;
    private final float speed;

    public LocalTtsConfig(String baseUrl, String speechEndpoint, String model, String voice, float speed) {
        this.baseUrl = TtsConfig.normalizeBaseUrl(baseUrl, TtsConfig.DEFAULT_LOCAL_BASE_URL);
        this.speechEndpoint = TtsConfig.normalizeEndpoint(speechEndpoint, TtsConfig.DEFAULT_SPEECH_ENDPOINT);
        this.model = TtsConfig.defaultIfBlank(model, TtsConfig.DEFAULT_LOCAL_MODEL);
        this.voice = TtsConfig.defaultIfBlank(voice, TtsConfig.DEFAULT_LOCAL_VOICE);
        this.speed = Math.max(0.25f, Math.min(4f, speed));
    }

    public String getSpeechUrl() {
        return baseUrl + speechEndpoint;
    }

    public String getModelsUrl() {
        return baseUrl + "/models";
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getSpeechEndpoint() {
        return speechEndpoint;
    }

    public String getModel() {
        return model;
    }

    public String getVoice() {
        return voice;
    }

    public float getSpeed() {
        return speed;
    }
}

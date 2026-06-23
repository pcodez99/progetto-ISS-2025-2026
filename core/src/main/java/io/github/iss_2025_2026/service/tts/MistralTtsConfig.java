package io.github.iss_2025_2026.service.tts;

public class MistralTtsConfig {
    private final String baseUrl;
    private final String speechEndpoint;
    private final String voicesEndpoint;
    private final String model;
    private final String voiceId;

    public MistralTtsConfig(String baseUrl, String speechEndpoint, String voicesEndpoint,
            String model, String voiceId) {
        this.baseUrl = TtsConfig.normalizeBaseUrl(baseUrl, TtsConfig.DEFAULT_MISTRAL_BASE_URL);
        this.speechEndpoint = TtsConfig.normalizeEndpoint(speechEndpoint, TtsConfig.DEFAULT_SPEECH_ENDPOINT);
        this.voicesEndpoint = TtsConfig.normalizeEndpoint(voicesEndpoint, TtsConfig.DEFAULT_VOICES_ENDPOINT);
        this.model = TtsConfig.defaultIfBlank(model, TtsConfig.DEFAULT_MISTRAL_MODEL);
        this.voiceId = voiceId != null ? voiceId.trim() : "";
    }

    public String getSpeechUrl() {
        return baseUrl + speechEndpoint;
    }

    public String getVoicesUrl() {
        return baseUrl + voicesEndpoint;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getSpeechEndpoint() {
        return speechEndpoint;
    }

    public String getVoicesEndpoint() {
        return voicesEndpoint;
    }

    public String getModel() {
        return model;
    }

    public String getVoiceId() {
        return voiceId;
    }
}

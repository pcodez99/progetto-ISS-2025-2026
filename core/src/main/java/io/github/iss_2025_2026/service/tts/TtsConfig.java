package io.github.iss_2025_2026.service.tts;

import io.github.iss_2025_2026.service.GameProperties;

public class TtsConfig {
    public static final boolean DEFAULT_ENABLED = true;
    public static final TtsProvider DEFAULT_PROVIDER = TtsProvider.LOCAL;
    public static final String DEFAULT_LOCAL_BASE_URL = "http://127.0.0.1:8000/v1";
    public static final String DEFAULT_SPEECH_ENDPOINT = "/audio/speech";
    public static final String DEFAULT_VOICES_ENDPOINT = "/audio/voices";
    public static final String DEFAULT_LOCAL_MODEL = "mlx-community/Voxtral-4B-TTS-2603-mlx-4bit";
    public static final String DEFAULT_LOCAL_VOICE = "it_male";
    public static final String DEFAULT_MISTRAL_BASE_URL = "https://api.mistral.ai/v1";
    public static final String DEFAULT_MISTRAL_MODEL = "voxtral-mini-tts-2603";
    public static final String DEFAULT_RESPONSE_FORMAT = "wav";
    public static final float DEFAULT_SPEED = 1f;
    public static final float DEFAULT_VOLUME = 1f;
    public static final float DEFAULT_GAIN = 4f;
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    public static final int DEFAULT_READ_TIMEOUT_MS = 120000;

    private final boolean enabled;
    private final TtsProvider provider;
    private final String responseFormat;
    private final float volume;
    private final float gain;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final LocalTtsConfig local;
    private final MistralTtsConfig mistral;

    public TtsConfig(boolean enabled, TtsProvider provider, String responseFormat, float volume,
            int connectTimeoutMs, int readTimeoutMs, LocalTtsConfig local, MistralTtsConfig mistral) {
        this(enabled, provider, responseFormat, volume, DEFAULT_GAIN,
                connectTimeoutMs, readTimeoutMs, local, mistral);
    }

    public TtsConfig(boolean enabled, TtsProvider provider, String responseFormat, float volume, float gain,
            int connectTimeoutMs, int readTimeoutMs, LocalTtsConfig local, MistralTtsConfig mistral) {
        this.enabled = enabled;
        this.provider = provider != null ? provider : DEFAULT_PROVIDER;
        this.responseFormat = defaultIfBlank(responseFormat, DEFAULT_RESPONSE_FORMAT).toLowerCase();
        this.volume = Math.max(0f, Math.min(1f, volume));
        this.gain = Math.max(1f, Math.min(8f, gain));
        this.connectTimeoutMs = Math.max(1, connectTimeoutMs);
        this.readTimeoutMs = Math.max(1, readTimeoutMs);
        this.local = local != null ? local : defaultLocal();
        this.mistral = mistral != null ? mistral : defaultMistral();
    }

    public static TtsConfig fromGameProperties() {
        return new TtsConfig(
                GameProperties.getBoolean(GameProperties.KEY_TTS_ENABLED, DEFAULT_ENABLED),
                resolveProvider(),
                GameProperties.getString(GameProperties.KEY_TTS_RESPONSE_FORMAT, DEFAULT_RESPONSE_FORMAT),
                GameProperties.getFloat(GameProperties.KEY_TTS_VOLUME, DEFAULT_VOLUME),
                GameProperties.getFloat(GameProperties.KEY_TTS_GAIN, DEFAULT_GAIN),
                GameProperties.getInt(GameProperties.KEY_TTS_CONNECT_TIMEOUT_MS, DEFAULT_CONNECT_TIMEOUT_MS),
                GameProperties.getInt(GameProperties.KEY_TTS_READ_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS),
                new LocalTtsConfig(
                        GameProperties.getString(GameProperties.KEY_TTS_BASE_URL, DEFAULT_LOCAL_BASE_URL),
                        GameProperties.getString(GameProperties.KEY_TTS_SPEECH_ENDPOINT, DEFAULT_SPEECH_ENDPOINT),
                        GameProperties.getString(GameProperties.KEY_TTS_MODEL, DEFAULT_LOCAL_MODEL),
                        GameProperties.getString(GameProperties.KEY_TTS_VOICE, DEFAULT_LOCAL_VOICE),
                        GameProperties.getFloat(GameProperties.KEY_TTS_SPEED, DEFAULT_SPEED)),
                new MistralTtsConfig(
                        GameProperties.getString(GameProperties.KEY_TTS_MISTRAL_BASE_URL,
                                DEFAULT_MISTRAL_BASE_URL),
                        GameProperties.getString(GameProperties.KEY_TTS_MISTRAL_SPEECH_ENDPOINT,
                                DEFAULT_SPEECH_ENDPOINT),
                        GameProperties.getString(GameProperties.KEY_TTS_MISTRAL_VOICES_ENDPOINT,
                                DEFAULT_VOICES_ENDPOINT),
                        GameProperties.getString(GameProperties.KEY_TTS_MISTRAL_MODEL,
                                DEFAULT_MISTRAL_MODEL),
                        GameProperties.getString(GameProperties.KEY_TTS_MISTRAL_VOICE_ID, "")));
    }

    private static TtsProvider resolveProvider() {
        String environmentProvider = System.getenv("TTS_PROVIDER");
        if (environmentProvider != null && !environmentProvider.trim().isEmpty()) {
            return TtsProvider.from(environmentProvider);
        }
        return TtsProvider.from(GameProperties.getString(GameProperties.KEY_TTS_PROVIDER,
                DEFAULT_PROVIDER.name()));
    }

    public boolean isEnabled() {
        return enabled;
    }

    public TtsProvider getProvider() {
        return provider;
    }

    public LocalTtsConfig getLocal() {
        return local;
    }

    public MistralTtsConfig getMistral() {
        return mistral;
    }

    public String getResponseFormat() {
        return responseFormat;
    }

    public float getVolume() {
        return volume;
    }

    public float getGain() {
        return gain;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    private static LocalTtsConfig defaultLocal() {
        return new LocalTtsConfig(DEFAULT_LOCAL_BASE_URL, DEFAULT_SPEECH_ENDPOINT,
                DEFAULT_LOCAL_MODEL, DEFAULT_LOCAL_VOICE, DEFAULT_SPEED);
    }

    private static MistralTtsConfig defaultMistral() {
        return new MistralTtsConfig(DEFAULT_MISTRAL_BASE_URL, DEFAULT_SPEECH_ENDPOINT,
                DEFAULT_VOICES_ENDPOINT, DEFAULT_MISTRAL_MODEL, "");
    }

    static String normalizeBaseUrl(String value, String fallback) {
        String normalized = defaultIfBlank(value, fallback);
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    static String normalizeEndpoint(String value, String fallback) {
        String normalized = defaultIfBlank(value, fallback);
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    static String defaultIfBlank(String value, String fallback) {
        return value == null || value.trim().isEmpty() ? fallback : value.trim();
    }
}

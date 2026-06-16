package io.github.iss_2025_2026.service.ai;

import io.github.iss_2025_2026.service.GameProperties;

public class AiConfig {
    public static final boolean DEFAULT_ENABLED = true;
    public static final String DEFAULT_BASE_URL = "http://localhost:11434";
    public static final String DEFAULT_MODEL = "llama3";
    public static final String DEFAULT_CHAT_ENDPOINT = "/api/chat";
    public static final boolean DEFAULT_STREAM = false;
    public static final int DEFAULT_CONNECT_TIMEOUT_MS = 5000;
    public static final int DEFAULT_READ_TIMEOUT_MS = 30000;
    public static final float DEFAULT_TEMPERATURE = 0.75f;
    public static final int DEFAULT_MAX_PROMPT_CHARS = 4000;
    public static final boolean DEFAULT_FALLBACK_TO_STATIC_DIALOGUE = true;

    private final boolean enabled;
    private final String baseUrl;
    private final String model;
    private final String chatEndpoint;
    private final boolean stream;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;
    private final float defaultTemperature;
    private final int maxPromptChars;
    private final boolean fallbackToStaticDialogue;

    public AiConfig(boolean enabled, String baseUrl, String model, String chatEndpoint, boolean stream,
            int connectTimeoutMs, int readTimeoutMs, float defaultTemperature, int maxPromptChars,
            boolean fallbackToStaticDialogue) {
        this.enabled = enabled;
        this.baseUrl = normalizeBaseUrl(baseUrl);
        this.model = isBlank(model) ? DEFAULT_MODEL : model.trim();
        this.chatEndpoint = normalizeEndpoint(chatEndpoint);
        this.stream = stream;
        this.connectTimeoutMs = Math.max(1, connectTimeoutMs);
        this.readTimeoutMs = Math.max(1, readTimeoutMs);
        this.defaultTemperature = Math.max(0f, defaultTemperature);
        this.maxPromptChars = Math.max(256, maxPromptChars);
        this.fallbackToStaticDialogue = fallbackToStaticDialogue;
    }

    public static AiConfig fromGameProperties() {
        return new AiConfig(
                GameProperties.getBoolean(GameProperties.KEY_AI_ENABLED, DEFAULT_ENABLED),
                GameProperties.getString(GameProperties.KEY_AI_BASE_URL, DEFAULT_BASE_URL),
                GameProperties.getString(GameProperties.KEY_AI_MODEL, DEFAULT_MODEL),
                GameProperties.getString(GameProperties.KEY_AI_CHAT_ENDPOINT, DEFAULT_CHAT_ENDPOINT),
                GameProperties.getBoolean(GameProperties.KEY_AI_STREAM, DEFAULT_STREAM),
                GameProperties.getInt(GameProperties.KEY_AI_CONNECT_TIMEOUT_MS, DEFAULT_CONNECT_TIMEOUT_MS),
                GameProperties.getInt(GameProperties.KEY_AI_READ_TIMEOUT_MS, DEFAULT_READ_TIMEOUT_MS),
                GameProperties.getFloat(GameProperties.KEY_AI_DEFAULT_TEMPERATURE, DEFAULT_TEMPERATURE),
                GameProperties.getInt(GameProperties.KEY_AI_MAX_PROMPT_CHARS, DEFAULT_MAX_PROMPT_CHARS),
                GameProperties.getBoolean(GameProperties.KEY_AI_FALLBACK_TO_STATIC_DIALOGUE,
                        DEFAULT_FALLBACK_TO_STATIC_DIALOGUE));
    }

    public String getChatUrl() {
        return baseUrl + chatEndpoint;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public String getModel() {
        return model;
    }

    public String getChatEndpoint() {
        return chatEndpoint;
    }

    public boolean isStream() {
        return stream;
    }

    public int getConnectTimeoutMs() {
        return connectTimeoutMs;
    }

    public int getReadTimeoutMs() {
        return readTimeoutMs;
    }

    public float getDefaultTemperature() {
        return defaultTemperature;
    }

    public int getMaxPromptChars() {
        return maxPromptChars;
    }

    public boolean isFallbackToStaticDialogue() {
        return fallbackToStaticDialogue;
    }

    private static String normalizeBaseUrl(String value) {
        String normalized = isBlank(value) ? DEFAULT_BASE_URL : value.trim();
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    private static String normalizeEndpoint(String value) {
        String normalized = isBlank(value) ? DEFAULT_CHAT_ENDPOINT : value.trim();
        return normalized.startsWith("/") ? normalized : "/" + normalized;
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

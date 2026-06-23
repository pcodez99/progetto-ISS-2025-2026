package io.github.iss_2025_2026.service.tts;

public enum TtsProvider {
    LOCAL,
    MISTRAL;

    public static TtsProvider from(String value) {
        if (value == null || value.trim().isEmpty()) {
            return LOCAL;
        }
        try {
            return valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Provider TTS non supportato: " + value, exception);
        }
    }
}

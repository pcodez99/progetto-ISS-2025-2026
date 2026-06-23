package io.github.iss_2025_2026.service.tts;

public abstract class TtsClientCreator {
    public final TtsClient createClient(TtsConfig config) {
        if (config == null) {
            throw new IllegalArgumentException("Configurazione TTS nulla.");
        }
        validate(config);
        return buildClient(config);
    }

    protected void validate(TtsConfig config) {
        if (!"wav".equals(config.getResponseFormat())) {
            throw new IllegalArgumentException("Il player NPC richiede response_format=wav.");
        }
    }

    protected abstract TtsClient buildClient(TtsConfig config);
}

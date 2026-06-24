package io.github.iss_2025_2026.service.tts;

public class LocalVoxtralTtsClientCreator extends TtsClientCreator {
    @Override
    protected TtsClient buildClient(TtsConfig config) {
        return new LocalVoxtralTtsClient(config);
    }
}

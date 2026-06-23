package io.github.iss_2025_2026.service.tts;

public interface TtsClient {
    TtsResult synthesize(TtsRequest request) throws TtsException;

    boolean isAvailable();
}

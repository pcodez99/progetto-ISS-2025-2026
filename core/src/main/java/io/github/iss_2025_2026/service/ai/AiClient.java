package io.github.iss_2025_2026.service.ai;

public interface AiClient {
    AiResponse chat(AiRequest request, AiConfig config) throws AiException;

    boolean isAvailable(AiConfig config);
}

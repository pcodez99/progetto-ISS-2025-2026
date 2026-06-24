package io.github.iss_2025_2026.service.ai;

public class AiResponse {
    private final String content;
    private final String model;
    private final String rawResponse;

    public AiResponse(String content, String model, String rawResponse) {
        this.content = content;
        this.model = model;
        this.rawResponse = rawResponse;
    }

    public String getContent() {
        return content;
    }

    public String getModel() {
        return model;
    }

    public String getRawResponse() {
        return rawResponse;
    }
}

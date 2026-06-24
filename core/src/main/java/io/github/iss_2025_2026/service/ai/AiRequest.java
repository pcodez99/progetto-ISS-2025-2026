package io.github.iss_2025_2026.service.ai;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class AiRequest {
    private String model;
    private Boolean stream;
    private Float temperature;
    private List<AiMessage> messages = new ArrayList<>();

    public AiRequest() {
    }

    public AiRequest(String model, List<AiMessage> messages, Float temperature, Boolean stream) {
        this.model = model;
        setMessages(messages);
        this.temperature = temperature;
        this.stream = stream;
    }

    public static AiRequest chat(String systemPrompt, String userPrompt, float temperature) {
        List<AiMessage> messages = new ArrayList<>();
        messages.add(AiMessage.system(systemPrompt));
        messages.add(AiMessage.user(userPrompt));
        return new AiRequest(null, messages, temperature, null);
    }

    public AiRequest withDefaults(AiConfig config) {
        AiRequest copy = new AiRequest();
        copy.setModel(isBlank(model) ? config.getModel() : model);
        copy.setStream(stream != null ? stream : config.isStream());
        copy.setTemperature(temperature != null ? temperature : config.getDefaultTemperature());
        copy.setMessages(trimmedMessages(config.getMaxPromptChars()));
        return copy;
    }

    public List<AiMessage> trimmedMessages(int maxPromptChars) {
        List<AiMessage> trimmed = new ArrayList<>();
        int remaining = Math.max(0, maxPromptChars);
        for (AiMessage message : messages) {
            String content = message.getContent() != null ? message.getContent() : "";
            if (content.length() > remaining) {
                content = content.substring(0, remaining);
            }
            trimmed.add(new AiMessage(message.getRole(), content));
            remaining -= content.length();
            if (remaining <= 0) {
                break;
            }
        }
        return trimmed;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Boolean getStream() {
        return stream;
    }

    public void setStream(Boolean stream) {
        this.stream = stream;
    }

    public Float getTemperature() {
        return temperature;
    }

    public void setTemperature(Float temperature) {
        this.temperature = temperature;
    }

    public List<AiMessage> getMessages() {
        return Collections.unmodifiableList(messages);
    }

    public void setMessages(List<AiMessage> messages) {
        this.messages = new ArrayList<>(messages != null ? messages : Collections.<AiMessage>emptyList());
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}

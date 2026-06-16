package io.github.iss_2025_2026.model;

public class DialoguePrompt {
    private final String prompt;
    private final float temperature;
    private final EvolutionPath path;

    public DialoguePrompt(String prompt, float temperature, EvolutionPath path) {
        this.prompt = prompt;
        this.temperature = temperature;
        this.path = path;
    }

    public String getPrompt() {
        return prompt;
    }

    public float getTemperature() {
        return temperature;
    }

    public EvolutionPath getPath() {
        return path;
    }
}

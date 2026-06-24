package io.github.iss_2025_2026.model;

public class DialogueProfile {
    private EvolutionPath path = EvolutionPath.BALANCED;
    private float temperature = 0.75f;
    private String tone = "neutrale";
    private String promptSuffix = "";

    public EvolutionPath getPath() {
        return path != null ? path : EvolutionPath.BALANCED;
    }

    public void setPath(EvolutionPath path) {
        this.path = path != null ? path : EvolutionPath.BALANCED;
    }

    public float getTemperature() {
        return temperature;
    }

    public void setTemperature(float temperature) {
        this.temperature = Math.max(0f, temperature);
    }

    public String getTone() {
        return tone;
    }

    public void setTone(String tone) {
        this.tone = tone;
    }

    public String getPromptSuffix() {
        return promptSuffix;
    }

    public void setPromptSuffix(String promptSuffix) {
        this.promptSuffix = promptSuffix;
    }
}

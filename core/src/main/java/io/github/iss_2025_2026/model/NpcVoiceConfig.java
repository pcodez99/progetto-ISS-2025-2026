package io.github.iss_2025_2026.model;

/**
 * Configurazione delle voci TTS associate a un NPC.
 */
public class NpcVoiceConfig {
    private String mistralVoiceId;
    private String localVoice;

    public NpcVoiceConfig() {
    }

    private NpcVoiceConfig(NpcVoiceConfig source) {
        this.mistralVoiceId = source.mistralVoiceId;
        this.localVoice = source.localVoice;
    }

    public NpcVoiceConfig copy() {
        return new NpcVoiceConfig(this);
    }

    public String getMistralVoiceId() {
        return mistralVoiceId;
    }

    public void setMistralVoiceId(String mistralVoiceId) {
        this.mistralVoiceId = mistralVoiceId;
    }

    public String getLocalVoice() {
        return localVoice;
    }

    public void setLocalVoice(String localVoice) {
        this.localVoice = localVoice;
    }
}

package io.github.iss_2025_2026.service.tts;

public class TtsResult {
    private final byte[] audioData;
    private final String contentType;

    public TtsResult(byte[] audioData, String contentType) {
        this.audioData = audioData != null ? audioData.clone() : new byte[0];
        this.contentType = contentType != null ? contentType : "application/octet-stream";
    }

    public byte[] getAudioData() {
        return audioData.clone();
    }

    public String getContentType() {
        return contentType;
    }
}

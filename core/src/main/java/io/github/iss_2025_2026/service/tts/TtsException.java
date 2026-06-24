package io.github.iss_2025_2026.service.tts;

public class TtsException extends Exception {
    public TtsException(String message) {
        super(message);
    }

    public TtsException(String message, Throwable cause) {
        super(message, cause);
    }
}

package io.github.iss_2025_2026.service.ai;

public class AiException extends Exception {
    public AiException(String message) {
        super(message);
    }

    public AiException(String message, Throwable cause) {
        super(message, cause);
    }
}

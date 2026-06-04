package io.github.iss_2025_2026.service;

/**
 * Esito del ripristino dopo un Game Over.
 */
public final class GameOverResult {
    public enum Status {
        RESTORED,
        NO_SAVE_FALLBACK,
        ERROR
    }

    private final Status status;
    private final String message;
    private final String saveFileName;

    private GameOverResult(Status status, String message, String saveFileName) {
        this.status = status;
        this.message = message;
        this.saveFileName = saveFileName;
    }

    public static GameOverResult restored(String message, String saveFileName) {
        return new GameOverResult(Status.RESTORED, message, saveFileName);
    }

    public static GameOverResult noSaveFallback(String message) {
        return new GameOverResult(Status.NO_SAVE_FALLBACK, message, null);
    }

    public static GameOverResult error(String message) {
        return new GameOverResult(Status.ERROR, message, null);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getSaveFileName() {
        return saveFileName;
    }

    public boolean isRestored() {
        return status == Status.RESTORED;
    }
}

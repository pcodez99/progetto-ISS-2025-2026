package io.github.iss_2025_2026.service;

/**
 * Result object returned by save operations so UI layers can show clear feedback.
 */
public final class SaveResult {
    public enum Status {
        SUCCESS,
        SKIPPED,
        INVALID,
        ERROR
    }

    private final Status status;
    private final String message;
    private final String fileName;

    private SaveResult(Status status, String message, String fileName) {
        this.status = status;
        this.message = message;
        this.fileName = fileName;
    }

    public static SaveResult success(String message, String fileName) {
        return new SaveResult(Status.SUCCESS, message, fileName);
    }

    public static SaveResult skipped(String message, String fileName) {
        return new SaveResult(Status.SKIPPED, message, fileName);
    }

    public static SaveResult invalid(String message, String fileName) {
        return new SaveResult(Status.INVALID, message, fileName);
    }

    public static SaveResult error(String message, String fileName) {
        return new SaveResult(Status.ERROR, message, fileName);
    }

    public Status getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getFileName() {
        return fileName;
    }

    public boolean isSuccess() {
        return status == Status.SUCCESS;
    }

    public boolean shouldNotifyPlayer() {
        return status != Status.SKIPPED;
    }
}

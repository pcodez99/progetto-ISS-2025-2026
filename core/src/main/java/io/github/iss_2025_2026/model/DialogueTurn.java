package io.github.iss_2025_2026.model;

public class DialogueTurn {
    private final String speaker;
    private final String text;
    private final boolean fromPlayer;
    private final long timestampMillis;

    public DialogueTurn(String speaker, String text, boolean fromPlayer) {
        this(speaker, text, fromPlayer, System.currentTimeMillis());
    }

    public DialogueTurn(String speaker, String text, boolean fromPlayer, long timestampMillis) {
        this.speaker = speaker;
        this.text = text;
        this.fromPlayer = fromPlayer;
        this.timestampMillis = timestampMillis;
    }

    public String getSpeaker() {
        return speaker;
    }

    public String getText() {
        return text;
    }

    public boolean isFromPlayer() {
        return fromPlayer;
    }

    public long getTimestampMillis() {
        return timestampMillis;
    }
}

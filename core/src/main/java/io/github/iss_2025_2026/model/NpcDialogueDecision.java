package io.github.iss_2025_2026.model;

public class NpcDialogueDecision {
    public static final int MIN_KARMA_DELTA = -10;
    public static final int MAX_KARMA_DELTA = 10;

    private final String reply;
    private final boolean endConversation;
    private final boolean badBehavior;
    private final int karmaDelta;
    private final ChoiceEventType choiceEventType;
    private final String reason;

    public NpcDialogueDecision(String reply, boolean endConversation, boolean badBehavior, int karmaDelta,
            ChoiceEventType choiceEventType, String reason) {
        this.reply = sanitizeReply(reply);
        this.endConversation = endConversation;
        this.badBehavior = badBehavior;
        this.karmaDelta = clampKarmaDelta(karmaDelta);
        this.choiceEventType = choiceEventType;
        this.reason = reason != null ? reason.trim() : "";
    }

    public static NpcDialogueDecision neutral(String reply) {
        return new NpcDialogueDecision(reply, false, false, 0, null, "");
    }

    public String getReply() {
        return reply;
    }

    public boolean isEndConversation() {
        return endConversation;
    }

    public boolean isBadBehavior() {
        return badBehavior;
    }

    public int getKarmaDelta() {
        return karmaDelta;
    }

    public ChoiceEventType getChoiceEventType() {
        return choiceEventType;
    }

    public String getReason() {
        return reason;
    }

    private static String sanitizeReply(String reply) {
        if (reply == null || reply.trim().isEmpty()) {
            return "Non so cosa rispondere.";
        }
        return reply.trim();
    }

    private static int clampKarmaDelta(int delta) {
        if (delta < MIN_KARMA_DELTA) {
            return MIN_KARMA_DELTA;
        }
        if (delta > MAX_KARMA_DELTA) {
            return MAX_KARMA_DELTA;
        }
        return delta;
    }
}

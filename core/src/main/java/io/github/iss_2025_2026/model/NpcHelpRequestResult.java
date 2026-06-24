package io.github.iss_2025_2026.model;

/** Risultato immutabile della risoluzione di una richiesta NPC. */
public final class NpcHelpRequestResult {
    private final boolean resolved;
    private final NpcHelpRequestOutcome outcome;
    private final String npcReply;
    private final String feedback;
    private final int karmaDelta;
    private final String rewardId;
    private final String rewardName;

    private NpcHelpRequestResult(boolean resolved, NpcHelpRequestOutcome outcome, String npcReply,
            String feedback, int karmaDelta, String rewardId, String rewardName) {
        this.resolved = resolved;
        this.outcome = outcome;
        this.npcReply = npcReply != null ? npcReply : "";
        this.feedback = feedback != null ? feedback : "";
        this.karmaDelta = karmaDelta;
        this.rewardId = rewardId;
        this.rewardName = rewardName;
    }

    public static NpcHelpRequestResult accepted(String npcReply, int karmaDelta, String rewardId,
            String rewardName) {
        String feedback = "+" + Math.max(0, karmaDelta) + " Karma · Ricevuto: " + rewardName;
        return new NpcHelpRequestResult(true, NpcHelpRequestOutcome.ACCEPTED, npcReply, feedback,
                karmaDelta, rewardId, rewardName);
    }

    public static NpcHelpRequestResult refused(String npcReply, int karmaDelta) {
        String feedback = karmaDelta + " Karma · Richiesta rifiutata";
        return new NpcHelpRequestResult(true, NpcHelpRequestOutcome.REFUSED, npcReply, feedback,
                karmaDelta, null, null);
    }

    public static NpcHelpRequestResult failure(String feedback) {
        return new NpcHelpRequestResult(false, null, "", feedback, 0, null, null);
    }

    public boolean isResolved() {
        return resolved;
    }

    public NpcHelpRequestOutcome getOutcome() {
        return outcome;
    }

    public String getNpcReply() {
        return npcReply;
    }

    public String getFeedback() {
        return feedback;
    }

    public int getKarmaDelta() {
        return karmaDelta;
    }

    public String getRewardId() {
        return rewardId;
    }

    public String getRewardName() {
        return rewardName;
    }
}

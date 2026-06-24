package io.github.iss_2025_2026.model;

/**
 * Configurazione della richiesta di aiuto proposta da un NPC durante il dialogo.
 */
public class NpcHelpRequest {
    private int triggerAfterNpcReplies = 1;
    private String text;
    private String acceptedReply;
    private String refusedReply;
    private boolean endConversationAfterChoice;

    public NpcHelpRequest() {
    }

    private NpcHelpRequest(NpcHelpRequest source) {
        this.triggerAfterNpcReplies = source.triggerAfterNpcReplies;
        this.text = source.text;
        this.acceptedReply = source.acceptedReply;
        this.refusedReply = source.refusedReply;
        this.endConversationAfterChoice = source.endConversationAfterChoice;
    }

    public NpcHelpRequest copy() {
        return new NpcHelpRequest(this);
    }

    public int getTriggerAfterNpcReplies() {
        return triggerAfterNpcReplies;
    }

    public void setTriggerAfterNpcReplies(int triggerAfterNpcReplies) {
        this.triggerAfterNpcReplies = Math.max(1, triggerAfterNpcReplies);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getAcceptedReply() {
        return acceptedReply;
    }

    public void setAcceptedReply(String acceptedReply) {
        this.acceptedReply = acceptedReply;
    }

    public String getRefusedReply() {
        return refusedReply;
    }

    public void setRefusedReply(String refusedReply) {
        this.refusedReply = refusedReply;
    }

    public boolean isEndConversationAfterChoice() {
        return endConversationAfterChoice;
    }

    public void setEndConversationAfterChoice(boolean endConversationAfterChoice) {
        this.endConversationAfterChoice = endConversationAfterChoice;
    }
}

package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.model.DialogueSession;
import io.github.iss_2025_2026.model.DialogueSessionState;
import io.github.iss_2025_2026.model.DialogueTurn;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.NpcDialogueDecision;
import io.github.iss_2025_2026.model.Player;

public class NpcDialogueController {
    public static final int DEFAULT_MAX_USER_INPUT_CHARS = 220;

    private final int maxUserInputChars;
    private DialogueSession activeSession;
    private int nextSessionId = 1;
    private String statusMessage = "";

    public NpcDialogueController() {
        this(DEFAULT_MAX_USER_INPUT_CHARS);
    }

    public NpcDialogueController(int maxUserInputChars) {
        this.maxUserInputChars = Math.max(20, maxUserInputChars);
    }

    public DialogueSession open(Player player, Npc npc) {
        activeSession = new DialogueSession(nextSessionId++, player, npc);
        statusMessage = "Scrivi e premi Invio. ESC chiude.";
        return activeSession;
    }

    public void close() {
        if (activeSession != null) {
            activeSession.setState(DialogueSessionState.CLOSED);
        }
        activeSession = null;
        statusMessage = "";
    }

    public boolean isActive() {
        return activeSession != null && activeSession.getState() != DialogueSessionState.CLOSED;
    }

    public boolean isWaitingForAi() {
        return activeSession != null && activeSession.isWaitingForAi();
    }

    public boolean isEnded() {
        return activeSession != null && activeSession.getState() == DialogueSessionState.ENDED;
    }

    public DialogueSession getActiveSession() {
        return activeSession;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public int getMaxUserInputChars() {
        return maxUserInputChars;
    }

    public DialogueRequestContext submitUserInput(String rawInput) {
        if (activeSession == null || !activeSession.isInputActive()) {
            return null;
        }

        String input = sanitize(rawInput);
        if (input.isEmpty()) {
            statusMessage = "Scrivi qualcosa prima di inviare.";
            return null;
        }

        activeSession.addTurn(new DialogueTurn(activeSession.getPlayer().getName(), input, true));
        activeSession.setState(DialogueSessionState.WAITING_AI);
        statusMessage = "Sta rispondendo...";

        return new DialogueRequestContext(
                activeSession.getId(),
                activeSession.getPlayer(),
                activeSession.getNpc(),
                input,
                activeSession.snapshotHistory());
    }

    public boolean completeNpcResponse(int sessionId, String response) {
        return completeNpcDecision(sessionId, NpcDialogueDecision.neutral(response));
    }

    public boolean completeNpcDecision(int sessionId, NpcDialogueDecision decision) {
        if (activeSession == null || activeSession.getId() != sessionId) {
            return false;
        }

        NpcDialogueDecision safeDecision = decision != null
                ? decision
                : NpcDialogueDecision.neutral(null);
        String safeResponse = sanitizeNpcResponse(safeDecision.getReply());
        activeSession.addTurn(new DialogueTurn(activeSession.getNpc().getName(), safeResponse, false));
        if (safeDecision.isEndConversation()) {
            activeSession.setState(DialogueSessionState.ENDED);
            statusMessage = "Dialogo concluso. ESC chiude.";
        } else {
            activeSession.setState(DialogueSessionState.INPUT_ACTIVE);
            statusMessage = "Scrivi e premi Invio. ESC chiude.";
        }
        return true;
    }

    private String sanitize(String rawInput) {
        if (rawInput == null) {
            return "";
        }
        String input = rawInput.trim();
        if (input.length() > maxUserInputChars) {
            input = input.substring(0, maxUserInputChars).trim();
        }
        return input;
    }

    private String sanitizeNpcResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            return "Non so cosa rispondere.";
        }
        return response.trim();
    }
}

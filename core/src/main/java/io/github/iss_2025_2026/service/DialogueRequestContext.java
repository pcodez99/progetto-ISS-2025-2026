package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.model.DialogueTurn;
import io.github.iss_2025_2026.model.Npc;
import io.github.iss_2025_2026.model.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DialogueRequestContext {
    private final int sessionId;
    private final Player player;
    private final Npc npc;
    private final String userInput;
    private final List<DialogueTurn> history;

    public DialogueRequestContext(int sessionId, Player player, Npc npc, String userInput,
            List<DialogueTurn> history) {
        this.sessionId = sessionId;
        this.player = player;
        this.npc = npc != null ? npc.copy() : null;
        this.userInput = userInput;
        this.history = history != null
                ? new ArrayList<>(history)
                : new ArrayList<DialogueTurn>();
    }

    public int getSessionId() {
        return sessionId;
    }

    public Player getPlayer() {
        return player;
    }

    public Npc getNpc() {
        return npc != null ? npc.copy() : null;
    }

    public String getUserInput() {
        return userInput;
    }

    public List<DialogueTurn> getHistory() {
        return Collections.unmodifiableList(history);
    }
}

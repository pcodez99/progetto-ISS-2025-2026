package io.github.iss_2025_2026.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DialogueSession {
    private final int id;
    private final Player player;
    private final Npc npc;
    private final List<DialogueTurn> history;
    private DialogueSessionState state;

    public DialogueSession(int id, Player player, Npc npc) {
        if (player == null) {
            throw new IllegalArgumentException("Il player del dialogo non puo essere nullo.");
        }
        if (npc == null) {
            throw new IllegalArgumentException("L'NPC del dialogo non puo essere nullo.");
        }
        this.id = id;
        this.player = player;
        this.npc = npc.copy();
        this.history = new ArrayList<>();
        this.state = DialogueSessionState.INPUT_ACTIVE;
    }

    public int getId() {
        return id;
    }

    public Player getPlayer() {
        return player;
    }

    public Npc getNpc() {
        return npc.copy();
    }

    public DialogueSessionState getState() {
        return state;
    }

    public void setState(DialogueSessionState state) {
        this.state = state != null ? state : DialogueSessionState.INPUT_ACTIVE;
    }

    public boolean isWaitingForAi() {
        return state == DialogueSessionState.WAITING_AI;
    }

    public boolean isInputActive() {
        return state == DialogueSessionState.INPUT_ACTIVE;
    }

    public void addTurn(DialogueTurn turn) {
        if (turn != null && turn.getText() != null && !turn.getText().trim().isEmpty()) {
            history.add(turn);
        }
    }

    public List<DialogueTurn> getHistory() {
        return Collections.unmodifiableList(history);
    }

    public List<DialogueTurn> snapshotHistory() {
        return new ArrayList<>(history);
    }
}

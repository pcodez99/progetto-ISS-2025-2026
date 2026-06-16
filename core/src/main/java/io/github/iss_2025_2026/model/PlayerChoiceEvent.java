package io.github.iss_2025_2026.model;

public class PlayerChoiceEvent {
    private ChoiceEventType type;
    private String npcId;

    public PlayerChoiceEvent() {
    }

    public PlayerChoiceEvent(ChoiceEventType type, String npcId) {
        this.type = type;
        this.npcId = npcId;
    }

    public static PlayerChoiceEvent of(ChoiceEventType type) {
        return new PlayerChoiceEvent(type, null);
    }

    public static PlayerChoiceEvent forNpc(ChoiceEventType type, String npcId) {
        return new PlayerChoiceEvent(type, npcId);
    }

    public ChoiceEventType getType() {
        return type;
    }

    public void setType(ChoiceEventType type) {
        this.type = type;
    }

    public String getNpcId() {
        return npcId;
    }

    public void setNpcId(String npcId) {
        this.npcId = npcId;
    }
}

package io.github.iss_2025_2026.model;

/**
 * POJO che rappresenta lo stato della partita da salvare su file JSON.
 * Include i riferimenti per ricostruire il personaggio tramite Factory.
 */
public class GameState {
    private String playerName;
    private String characterType; // Es: "MAMMA", "PAPA", "NONNO" (per la Factory)
    private int hp;
    private int baseDamage;
    private int level;
    private Backpack backpack; // Jackson serializzerà l'intero oggetto Backpack
    private String lastCheckpoint;
    private String specialAbilityId; // ID per la AbilityFactory (Es: "HEAL")

    // Costruttore vuoto obbligatorio per Jackson
    public GameState() {
    }

    // Costruttore completo per creare il salvataggio velocemente
    public GameState(String playerName, String characterType, int hp, int baseDamage,
            int level, Backpack backpack, String lastCheckpoint, String specialAbilityId) {
        this.playerName = playerName;
        this.characterType = characterType;
        this.hp = hp;
        this.baseDamage = baseDamage;
        this.level = level;
        this.backpack = backpack;
        this.lastCheckpoint = lastCheckpoint;
        this.specialAbilityId = specialAbilityId;
    }

    // --- GETTER E SETTER ---

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getCharacterType() {
        return characterType;
    }

    public void setCharacterType(String characterType) {
        this.characterType = characterType;
    }

    public int getHp() {
        return hp;
    }

    public void setHp(int hp) {
        this.hp = hp;
    }

    public int getBaseDamage() {
        return baseDamage;
    }

    public void setBaseDamage(int baseDamage) {
        this.baseDamage = baseDamage;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Backpack getBackpack() {
        return backpack;
    }

    public void setBackpack(Backpack backpack) {
        this.backpack = backpack;
    }

    public String getLastCheckpoint() {
        return lastCheckpoint;
    }

    public void setLastCheckpoint(String lastCheckpoint) {
        this.lastCheckpoint = lastCheckpoint;
    }

    public String getSpecialAbilityId() {
        return specialAbilityId;
    }

    public void setSpecialAbilityId(String specialAbilityId) {
        this.specialAbilityId = specialAbilityId;
    }
}

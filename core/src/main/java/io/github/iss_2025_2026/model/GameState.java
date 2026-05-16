package io.github.iss_2025_2026.model;

/**
 * Stato serializzabile della partita.
 * Supporta sia il formato attuale a uno o due player, sia il vecchio formato legacy.
 */
public class GameState {
    private String gameName;
    private NewGameConfigModel.GameMode gameMode;
    private PlayerSaveState playerOne;
    private PlayerSaveState playerTwo;
    private String savedAt;

    // Legacy single-player fields kept for backward compatibility.
    private String playerName;
    private String characterType;
    private int hp;
    private int baseDamage;
    private int level;
    private Backpack backpack;
    private String lastCheckpoint;
    private String specialAbilityId;

    public GameState() {
    }

    public GameState(String gameName, NewGameConfigModel.GameMode gameMode, PlayerSaveState playerOne,
            PlayerSaveState playerTwo, String savedAt) {
        this.gameName = gameName;
        this.gameMode = gameMode;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.savedAt = savedAt;
    }

    public boolean hasLegacySinglePlayerData() {
        return playerOne == null && characterType != null && !characterType.trim().isEmpty();
    }

    public String getGameName() {
        return gameName;
    }

    public void setGameName(String gameName) {
        this.gameName = gameName;
    }

    public NewGameConfigModel.GameMode getGameMode() {
        return gameMode;
    }

    public void setGameMode(NewGameConfigModel.GameMode gameMode) {
        this.gameMode = gameMode;
    }

    public PlayerSaveState getPlayerOne() {
        return playerOne;
    }

    public void setPlayerOne(PlayerSaveState playerOne) {
        this.playerOne = playerOne;
    }

    public PlayerSaveState getPlayerTwo() {
        return playerTwo;
    }

    public void setPlayerTwo(PlayerSaveState playerTwo) {
        this.playerTwo = playerTwo;
    }

    public String getSavedAt() {
        return savedAt;
    }

    public void setSavedAt(String savedAt) {
        this.savedAt = savedAt;
    }

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

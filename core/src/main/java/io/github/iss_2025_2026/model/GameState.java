package io.github.iss_2025_2026.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Stato serializzabile della partita.
 * Supporta sia il formato attuale a uno o due player, sia il vecchio formato legacy.
 */
public class GameState {
    public enum SaveType {
        MANUAL,
        AUTO
    }

    public enum Phase {
        MENU,
        LOADING_LEVEL,
        PLAYING,
        COMBAT,
        LEVEL_COMPLETED,
        GAME_COMPLETED
    }

    private static final int DEFAULT_LEVEL_ID = 1;
    private static final int CURRENT_SCHEMA_VERSION = 2;

    private int schemaVersion = CURRENT_SCHEMA_VERSION;
    private SaveType saveType = SaveType.MANUAL;
    private String gameName;
    private NewGameConfigModel.GameMode gameMode;
    private PlayerSaveState playerOne;
    private PlayerSaveState playerTwo;
    private String savedAt;
    private int currentLevelId = DEFAULT_LEVEL_ID;
    private Phase phase = Phase.MENU;
    private List<Integer> completedLevelIds = new ArrayList<>();
    private int lastCheckpointLevelId;

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
        this.currentLevelId = DEFAULT_LEVEL_ID;
        this.phase = Phase.MENU;
    }

    public boolean hasLegacySinglePlayerData() {
        return playerOne == null && characterType != null && !characterType.trim().isEmpty();
    }

    public int getSchemaVersion() {
        return schemaVersion > 0 ? schemaVersion : 1;
    }

    public void setSchemaVersion(int schemaVersion) {
        this.schemaVersion = schemaVersion > 0 ? schemaVersion : 1;
    }

    public SaveType getSaveType() {
        return saveType != null ? saveType : SaveType.MANUAL;
    }

    public void setSaveType(SaveType saveType) {
        this.saveType = saveType != null ? saveType : SaveType.MANUAL;
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

    public int getCurrentLevelId() {
        return currentLevelId > 0 ? currentLevelId : DEFAULT_LEVEL_ID;
    }

    public void setCurrentLevelId(int currentLevelId) {
        this.currentLevelId = currentLevelId > 0 ? currentLevelId : DEFAULT_LEVEL_ID;
    }

    public Phase getPhase() {
        return phase != null ? phase : Phase.MENU;
    }

    public void setPhase(Phase phase) {
        this.phase = phase != null ? phase : Phase.MENU;
    }

    public List<Integer> getCompletedLevelIds() {
        return Collections.unmodifiableList(completedLevelIds);
    }

    public void setCompletedLevelIds(List<Integer> completedLevelIds) {
        this.completedLevelIds = new ArrayList<>(
                completedLevelIds != null ? completedLevelIds : Collections.<Integer>emptyList());
    }

    public void addCompletedLevelId(int levelId) {
        if (!completedLevelIds.contains(levelId)) {
            completedLevelIds.add(levelId);
        }
    }

    public void clearCompletedLevels() {
        completedLevelIds.clear();
    }

    public String getLastCheckpointId() {
        return lastCheckpoint;
    }

    public void setLastCheckpointId(String lastCheckpointId) {
        this.lastCheckpoint = lastCheckpointId;
    }

    public int getLastCheckpointLevelId() {
        return lastCheckpointLevelId;
    }

    public void setLastCheckpointLevelId(int lastCheckpointLevelId) {
        this.lastCheckpointLevelId = Math.max(0, lastCheckpointLevelId);
    }

    public boolean isLastCheckpoint(String checkpointId, int levelId) {
        return checkpointId != null
                && checkpointId.equals(lastCheckpoint)
                && lastCheckpointLevelId == levelId;
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

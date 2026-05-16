package io.github.iss_2025_2026.model;

public class NewGameConfigModel {
    public enum GameMode {
        SINGLE_PLAYER,
        MULTIPLAYER
    }

    private String gameName;
    private GameMode gameMode;
    private String selectedCharacterPlayerOne;
    private String selectedCharacterPlayerTwo;

    public NewGameConfigModel() {
        this.gameName = "Nuova Partita";
        this.gameMode = GameMode.SINGLE_PLAYER;
        this.selectedCharacterPlayerOne = null;
        this.selectedCharacterPlayerTwo = null;
    }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public GameMode getGameMode() { return gameMode; }
    public void setGameMode(GameMode gameMode) { this.gameMode = gameMode; }

    public String getSelectedCharacterPlayerOne() {
        return selectedCharacterPlayerOne;
    }

    public void setSelectedCharacterPlayerOne(String selectedCharacterPlayerOne) {
        this.selectedCharacterPlayerOne = selectedCharacterPlayerOne;
    }

    public String getSelectedCharacterPlayerTwo() {
        return selectedCharacterPlayerTwo;
    }

    public void setSelectedCharacterPlayerTwo(String selectedCharacterPlayerTwo) {
        this.selectedCharacterPlayerTwo = selectedCharacterPlayerTwo;
    }

    public boolean isMultiplayer() {
        return gameMode == GameMode.MULTIPLAYER;
    }

    public void clearCharacterSelections() {
        this.selectedCharacterPlayerOne = null;
        this.selectedCharacterPlayerTwo = null;
    }
}

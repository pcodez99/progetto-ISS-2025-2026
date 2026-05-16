package io.github.iss_2025_2026.model;

public class NewGameConfigModel {
    public enum GameMode {
        SINGLE_PLAYER,
        MULTIPLAYER
    }

    private String gameName;
    private GameMode gameMode;

    public NewGameConfigModel() {
        this.gameName = "Nuova Partita";
        this.gameMode = GameMode.SINGLE_PLAYER;
    }

    public String getGameName() { return gameName; }
    public void setGameName(String gameName) { this.gameName = gameName; }

    public GameMode getGameMode() { return gameMode; }
    public void setGameMode(GameMode gameMode) { this.gameMode = gameMode; }
}

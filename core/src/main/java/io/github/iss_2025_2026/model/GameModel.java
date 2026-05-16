package io.github.iss_2025_2026.model;

/**
 * Game Model (Parte del pattern MVC).
 * Rappresenta lo stato del gioco e la logica di business.
 * Non contiene riferimenti a LibGDX per il rendering o l'input.
 */
public class GameModel {
    private String message;
    private float timer;
    private String gameName;
    private NewGameConfigModel.GameMode gameMode;
    private Player playerOne;
    private Player playerTwo;
    private boolean gameStarted;
    private String currentSaveFileName;

    public GameModel() {
        this.message = "Viddani VS Alieni - MVC Base Architecture";
        this.timer = 0;
        this.gameName = "Nuova Partita";
        this.gameMode = NewGameConfigModel.GameMode.SINGLE_PLAYER;
        this.playerOne = null;
        this.playerTwo = null;
        this.gameStarted = false;
        this.currentSaveFileName = null;
    }

    public void update(float delta) {
        timer += delta;
    }

    public String getMessage() {
        return message;
    }

    public float getTimer() {
        return timer;
    }

    public String getGameName() {
        return gameName;
    }

    public NewGameConfigModel.GameMode getGameMode() {
        return gameMode;
    }

    public Player getPlayerOne() {
        return playerOne;
    }

    public Player getPlayerTwo() {
        return playerTwo;
    }

    public boolean hasGameStarted() {
        return gameStarted;
    }

    public String getCurrentSaveFileName() {
        return currentSaveFileName;
    }

    public boolean isMultiplayerGame() {
        return gameMode == NewGameConfigModel.GameMode.MULTIPLAYER && playerTwo != null;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setCurrentSaveFileName(String currentSaveFileName) {
        this.currentSaveFileName = currentSaveFileName;
    }

    public void startSinglePlayerGame(String gameName, Player playerOne) {
        this.gameName = gameName;
        this.gameMode = NewGameConfigModel.GameMode.SINGLE_PLAYER;
        this.playerOne = playerOne;
        this.playerTwo = null;
        this.timer = 0f;
        this.gameStarted = true;
        this.currentSaveFileName = null;
        this.message = "Partita avviata: " + playerOne.getName() + " e pronto a combattere.";
    }

    public void startMultiplayerGame(String gameName, Player playerOne, Player playerTwo) {
        this.gameName = gameName;
        this.gameMode = NewGameConfigModel.GameMode.MULTIPLAYER;
        this.playerOne = playerOne;
        this.playerTwo = playerTwo;
        this.timer = 0f;
        this.gameStarted = true;
        this.currentSaveFileName = null;
        this.message = "Partita multiplayer avviata: " + playerOne.getName() + " e " + playerTwo.getName() + ".";
    }
}

package io.github.iss_2025_2026.controller;

import io.github.iss_2025_2026.map.TmxMapContract;
import io.github.iss_2025_2026.model.GameState;

/**
 * MVC controller for game progression.
 * It coordinates level transitions without parsing maps, loading assets, or evaluating objectives directly.
 */
public final class GameFlowController {
    private final GameState gameState;
    private final SceneController sceneController;

    public GameFlowController(GameState gameState, SceneController sceneController) {
        this.gameState = gameState;
        this.sceneController = sceneController;
    }

    public void startCurrentLevel() {
        int levelId = gameState.getCurrentLevelId();
        if (!sceneController.hasLevel(levelId)) {
            throw new IllegalArgumentException("Livello non valido o inesistente: " + levelId);
        }

        gameState.setPhase(GameState.Phase.LOADING_LEVEL);
        sceneController.loadLevel(levelId);
        gameState.setPhase(GameState.Phase.PLAYING);
    }

    public void startNewRun() {
        gameState.setCurrentLevelId(TmxMapContract.DEFAULT_LEVEL_ID);
        gameState.clearCompletedLevels();
        startCurrentLevel();
    }

    public void completeCurrentLevel() {
        int completedLevelId = gameState.getCurrentLevelId();
        gameState.addCompletedLevelId(completedLevelId);

        int nextLevelId = completedLevelId + 1;
        if (!sceneController.hasLevel(nextLevelId)) {
            gameState.setPhase(GameState.Phase.GAME_COMPLETED);
            return;
        }

        gameState.setCurrentLevelId(nextLevelId);
        gameState.setPhase(GameState.Phase.LEVEL_COMPLETED);
        // Il caricamento effettivo del livello successivo avviene quando
        // la LevelCompletedScreen chiama startCurrentLevel()
    }
}

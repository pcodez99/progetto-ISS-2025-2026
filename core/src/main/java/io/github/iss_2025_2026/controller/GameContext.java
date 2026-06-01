package io.github.iss_2025_2026.controller;

import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.service.GameOverService;

/**
 * Persistent MVC runtime context.
 * It wires the shared model with controllers that survive screen changes.
 */
public final class GameContext {
    private final GameModel model;
    private final GameController gameController;
    private final SceneController sceneController;
    private final GameFlowController flowController;

    public GameContext(Main game) {
        this.model = new GameModel();
        this.gameController = new GameController(model);
        this.sceneController = new SceneController(this, game, model, gameController);
        this.flowController = new GameFlowController(model.getGameState(), sceneController);
    }

    public GameOverController createGameOverController() {
        return new GameOverController(
                model,
                new GameOverService(),
                flowController::startCurrentLevel);
    }

    public GameModel getModel() {
        return model;
    }

    public GameController getController() {
        return gameController;
    }

    public SceneController getSceneController() {
        return sceneController;
    }

    public GameFlowController getFlowController() {
        return flowController;
    }
}

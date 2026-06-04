package io.github.iss_2025_2026.controller;

import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.service.GameOverResult;
import io.github.iss_2025_2026.service.GameOverService;

/**
 * Controller MVC per la schermata di Game Over.
 */
public class GameOverController {
    public interface LevelResumeAction {
        void resumeLevel();
    }

    private final GameModel model;
    private final GameOverService gameOverService;
    private final LevelResumeAction levelResumeAction;

    public GameOverController(GameModel model, GameOverService gameOverService, LevelResumeAction levelResumeAction) {
        this.model = model;
        this.gameOverService = gameOverService;
        this.levelResumeAction = levelResumeAction;
    }

    public GameModel getModel() {
        return model;
    }

    public GameOverResult onContinue() {
        GameOverResult result = gameOverService.restoreFromLastSave(model);
        if (result.getStatus() != GameOverResult.Status.ERROR) {
            levelResumeAction.resumeLevel();
        } else {
            model.setMessage(result.getMessage());
        }
        return result;
    }
}

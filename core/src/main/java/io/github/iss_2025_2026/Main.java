package io.github.iss_2025_2026;

import com.badlogic.gdx.Game;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.controller.MainMenuController;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.MainMenuModel;
import io.github.iss_2025_2026.view.MainMenuScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    @Override
    public void create() {
        // Initialize global MVC components
        GameModel gameModel = new GameModel();
        GameController gameController = new GameController(gameModel);

        // Set the initial screen (the View), passing the game instance and global state
        setScreen(new MainMenuScreen(this, gameModel, gameController));
    }
}
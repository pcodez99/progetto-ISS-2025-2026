package io.github.iss_2025_2026;

import com.badlogic.gdx.Game;
import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.view.MainMenuScreen;
import io.github.iss_2025_2026.view.TestScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    @Override
    public void create() {
        // Initialize MVC components
        GameModel model = new GameModel();
        GameController controller = new GameController(model);

        // Set the initial screen (the View)
        setScreen(new MainMenuScreen());
    }
}
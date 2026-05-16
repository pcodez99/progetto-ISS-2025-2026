package io.github.iss_2025_2026;

import com.badlogic.gdx.Game;
import io.github.iss_2025_2026.controller.MainMenuController;
import io.github.iss_2025_2026.model.MainMenuModel;
import io.github.iss_2025_2026.view.MainMenuScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    @Override
    public void create() {
        // Initialize MVC components for Main Menu
        MainMenuModel mainMenuModel = new MainMenuModel();
        MainMenuController mainMenuController = new MainMenuController();


        // Set the initial screen (the View)
        setScreen(new MainMenuScreen(mainMenuModel, mainMenuController));
    }
}
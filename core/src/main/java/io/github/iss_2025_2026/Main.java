package io.github.iss_2025_2026;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.view.MainMenuScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    @Override
    public void create() {

        String charactersPath = "configs/characters.yaml";
        String abilitiesPath = "configs/abilities/";

        if (!Gdx.files.internal(charactersPath).exists() || !Gdx.files.internal(abilitiesPath).exists()) {
            Gdx.app.error("CharacterFactory", "CRITICAL: characters.yaml or abilities.yaml NOT FOUND at "
                    + charactersPath + " or " + abilitiesPath);
            // TO DO: AGGIUNGERE WARNING E CHIUDERE IL GIOCO SE MANCANO LE CONFIGS
            return;
        }

        // Initialize global MVC components
        GameModel gameModel = new GameModel();
        GameController gameController = new GameController(gameModel);

        // Set the initial screen (the View), passing the game instance and global state
        setScreen(new MainMenuScreen(this, gameModel, gameController));
    }
}

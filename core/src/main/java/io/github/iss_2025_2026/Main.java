package io.github.iss_2025_2026;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

import io.github.iss_2025_2026.controller.GameController;
import io.github.iss_2025_2026.factory.CharacterFactory;
import io.github.iss_2025_2026.map.GameStartupValidator;
import io.github.iss_2025_2026.map.LevelValidationResult;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.view.MainMenuScreen;
import io.github.iss_2025_2026.view.TestScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    @Override
    public void create() {
        LevelValidationResult mapValidation = GameStartupValidator.validateRuntimeAssets();
        if (!mapValidation.isValid()) {
            Gdx.app.error("GameStartup", mapValidation.toUserMessage());
            throw new GdxRuntimeException(mapValidation.toUserMessage());
        }

        // Support launching directly into the test map with a specific character via system
        // property "character"
        String characterProp = System.getProperty("character");
        if (characterProp != null && !characterProp.isEmpty()) {
            // Map common aliases (like "child" -> "bambino", "father" -> "papa", "mom" -> "mamma")
            String mappedId = characterProp.toLowerCase();
            if ("child".equals(mappedId)) mappedId = "bambino";
            if ("father".equals(mappedId)) mappedId = "papa";
            if ("mom".equals(mappedId)) mappedId = "mamma";

            // Create a player for the specified character and start directly in TestScreen
            CharacterFactory factory = new CharacterFactory();
            Player player = factory.createPlayer(mappedId);
            if (player != null) {
                GameModel testMapModel = new GameModel();
                testMapModel.startSinglePlayerGame("Campagna", player);
                GameController testMapController = new GameController(testMapModel);
                setScreen(new TestScreen(this, testMapModel, testMapController));
                return; // Skip default menu
            }
        }

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

    @Override
    public void dispose() {
        super.dispose();
    }
}

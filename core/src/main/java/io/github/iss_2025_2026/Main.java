package io.github.iss_2025_2026;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.GdxRuntimeException;

import io.github.iss_2025_2026.factory.PlayerFactory;
import io.github.iss_2025_2026.factory.YamlPlayerFactory;
import io.github.iss_2025_2026.map.GameStartupValidator;
import io.github.iss_2025_2026.map.LevelValidationResult;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.controller.GameContext;
import io.github.iss_2025_2026.view.MainMenuScreen;

/**
 * {@link com.badlogic.gdx.ApplicationListener} implementation shared by all
 * platforms.
 */
public class Main extends Game {
    private GameContext gameContext;

    @Override
    public void create() {
        LevelValidationResult mapValidation = GameStartupValidator.validateRuntimeAssets();
        if (!mapValidation.isValid()) {
            Gdx.app.error("GameStartup", mapValidation.toUserMessage());
            throw new GdxRuntimeException(mapValidation.toUserMessage());
        }

        String charactersPath = "configs/characters.yaml";
        String enemiesPath = "configs/enemies.yaml";
        String abilitiesPath = "configs/abilities/";

        if (!Gdx.files.internal(charactersPath).exists()
                || !Gdx.files.internal(enemiesPath).exists()
                || !Gdx.files.internal(abilitiesPath).exists()) {
            Gdx.app.error("CharacterConfig", "CRITICAL: required character configs NOT FOUND at "
                    + charactersPath + ", " + enemiesPath + " or " + abilitiesPath);
            // TO DO: AGGIUNGERE WARNING E CHIUDERE IL GIOCO SE MANCANO LE CONFIGS
            return;
        }

        gameContext = new GameContext(this);

        // Support launching directly into the test map with a specific character via system
        // property "character"
        String characterProp = System.getProperty("character");
        if (characterProp != null && !characterProp.isEmpty()) {
            // Map common aliases (like "child" -> "bambino", "father" -> "papa", "mom" -> "mamma")
            String mappedId = characterProp.toLowerCase();
            if ("child".equals(mappedId)) mappedId = "bambino";
            if ("father".equals(mappedId)) mappedId = "papa";
            if ("mom".equals(mappedId)) mappedId = "mamma";

            // Create a player for the specified character and start directly in the runtime level scene.
            PlayerFactory playerFactory = new YamlPlayerFactory();
            Player player = playerFactory.create(mappedId);
            if (player != null) {
                gameContext.getModel().startSinglePlayerGame("Campagna", player);
                gameContext.getFlowController().startCurrentLevel();
                return; // Skip default menu
            }
        }

        // Set the initial screen (the View), passing the game instance and global state
        setScreen(new MainMenuScreen(this, gameContext.getModel(), gameContext.getController()));
    }

    public GameContext getGameContext() {
        return gameContext;
    }

    @Override
    public void dispose() {
        super.dispose();
    }
}

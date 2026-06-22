package io.github.iss_2025_2026.controller;

import com.badlogic.gdx.Gdx;
import io.github.iss_2025_2026.Main;
import io.github.iss_2025_2026.factory.PlayerFactory;
import io.github.iss_2025_2026.model.CharacterSelectionModel;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.NewGameConfigModel;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.service.GameSaveService;
import io.github.iss_2025_2026.view.PlayerSelectionTransitionScreen;

import java.io.IOException;

public class CharacterSelectionController {
    private final Main game;
    private final GameModel gameModel;
    private final GameController gameController;
    private final CharacterSelectionModel model;
    private final PlayerFactory playerFactory;
    private final NewGameConfigModel config;
    private final int currentPlayerIndex;

    public CharacterSelectionController(Main game, GameModel gameModel, GameController gameController,
            CharacterSelectionModel model, PlayerFactory playerFactory, NewGameConfigModel config, int currentPlayerIndex) {
        this.game = game;
        this.gameModel = gameModel;
        this.gameController = gameController;
        this.model = model;
        this.playerFactory = playerFactory;
        this.config = config;
        this.currentPlayerIndex = currentPlayerIndex;
    }

    public void selectNext() {
        model.nextCharacter();
    }

    public void selectPrevious() {
        model.previousCharacter();
    }

    public void confirmSelection() {
        String characterId = model.getSelectedCharacter().getId();

        if (config.isMultiplayer()) {
            handleMultiplayerSelection(characterId);
            return;
        }

        config.setSelectedCharacterPlayerOne(characterId);
        Player playerOne = playerFactory.create(characterId);
        gameModel.startSinglePlayerGame(config.getGameName(), playerOne);
        persistNewRun();
        game.getGameContext().getFlowController().startCurrentLevel();
    }

    private void handleMultiplayerSelection(String characterId) {
        if (currentPlayerIndex == 1) {
            config.setSelectedCharacterPlayerOne(characterId);
            game.setScreen(new PlayerSelectionTransitionScreen(game, gameModel, gameController, config, 2));
            return;
        }

        if (config.getSelectedCharacterPlayerOne() == null || config.getSelectedCharacterPlayerOne().trim().isEmpty()) {
            game.setScreen(new PlayerSelectionTransitionScreen(game, gameModel, gameController, config, 2));
            return;
        }

        config.setSelectedCharacterPlayerTwo(characterId);

        Player playerOne = playerFactory.create(config.getSelectedCharacterPlayerOne());
        Player playerTwo = playerFactory.create(config.getSelectedCharacterPlayerTwo());
        gameModel.startMultiplayerGame(config.getGameName(), playerOne, playerTwo);
        persistNewRun();
        game.getGameContext().getFlowController().startCurrentLevel();
    }

    private void persistNewRun() {
        try {
            GameSaveService.saveCurrentGame(gameModel);
        } catch (IOException e) {
            Gdx.app.error("CharacterSelectionController", "Impossibile salvare la nuova partita.", e);
        }
    }
}

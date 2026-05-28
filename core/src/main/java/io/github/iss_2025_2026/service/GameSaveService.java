package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.factory.CharacterFactory;
import io.github.iss_2025_2026.factory.YamlCharacterFactory;
import io.github.iss_2025_2026.model.Backpack;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.NewGameConfigModel;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.PlayerSaveState;
import io.github.iss_2025_2026.persistence.SaveManager;

import java.io.IOException;
import java.time.Instant;

/**
 * Traduce il GameModel verso il formato di salvataggio e viceversa.
 */
public final class GameSaveService {

    private GameSaveService() {
    }

    public static void saveCurrentGame(GameModel model) throws IOException {
        if (model == null || !model.hasGameStarted() || model.getPlayerOne() == null) {
            throw new IOException("Nessuna partita avviata da salvare.");
        }

        String saveFileName = SaveManager.toSaveFileName(model.getGameName());
        SaveManager.saveGame(toGameState(model), saveFileName);
        model.setCurrentSaveFileName(saveFileName);
    }

    public static void loadGameIntoModel(GameModel model, String fileName) throws IOException {
        GameState state = SaveManager.loadGame(fileName);
        loadGameIntoModel(model, state);
        model.setCurrentSaveFileName(SaveManager.toSaveFileName(fileName));
    }

    public static GameState toGameState(GameModel model) {
        GameState state = new GameState(
                model.getGameName(),
                model.getGameMode(),
                toPlayerSaveState(model.getPlayerOne()),
                toPlayerSaveState(model.getPlayerTwo()),
                Instant.now().toString());
        state.setCurrentLevelId(model.getGameState().getCurrentLevelId());
        state.setPhase(model.getGameState().getPhase());
        state.setCompletedLevelIds(model.getGameState().getCompletedLevelIds());
        return state;
    }

    private static void loadGameIntoModel(GameModel model, GameState state) throws IOException {
        CharacterFactory factory = new YamlCharacterFactory();

        if (state.getPlayerOne() != null) {
            Player playerOne = restorePlayer(factory, state.getPlayerOne());
            Player playerTwo = state.getPlayerTwo() != null ? restorePlayer(factory, state.getPlayerTwo()) : null;
            String gameName = resolveGameName(state);
            if ((state.getGameMode() == NewGameConfigModel.GameMode.MULTIPLAYER || playerTwo != null)
                    && playerTwo != null) {
                model.startMultiplayerGame(gameName, playerOne, playerTwo);
                restoreProgress(model, state);
                return;
            }
            model.startSinglePlayerGame(gameName, playerOne);
            restoreProgress(model, state);
            return;
        }

        if (state.hasLegacySinglePlayerData()) {
            Player legacyPlayer = factory.createPlayer(normalizeCharacterId(state.getCharacterType()));
            legacyPlayer.restoreState(
                    state.getPlayerName(),
                    state.getHp(),
                    Math.max(legacyPlayer.getMaxHp(), state.getHp()),
                    Math.max(legacyPlayer.getBaseDamage(), state.getBaseDamage()),
                    Math.max(1, state.getLevel()),
                    0,
                    state.getBackpack());
            model.startSinglePlayerGame(resolveGameName(state), legacyPlayer);
            restoreProgress(model, state);
            return;
        }

        throw new IOException("Il salvataggio selezionato non contiene dati giocabili.");
    }

    private static void restoreProgress(GameModel model, GameState state) {
        model.getGameState().setCurrentLevelId(state.getCurrentLevelId());
        model.getGameState().setPhase(state.getPhase());
        model.getGameState().setCompletedLevelIds(state.getCompletedLevelIds());
    }

    private static PlayerSaveState toPlayerSaveState(Player player) {
        if (player == null) {
            return null;
        }

        Backpack backpack = player.getBackpack();
        return new PlayerSaveState(
                player.getCharacterId(),
                player.getName(),
                player.getHp(),
                player.getMaxHp(),
                player.getBaseDamage(),
                player.getLevel(),
                player.getKarma(),
                backpack);
    }

    private static Player restorePlayer(CharacterFactory factory, PlayerSaveState playerState) throws IOException {
        String characterId = normalizeCharacterId(playerState.getCharacterId());
        if (characterId == null || characterId.isEmpty()) {
            throw new IOException("Salvataggio corrotto: characterId mancante.");
        }

        Player player = factory.createPlayer(characterId);
        player.restoreState(
                playerState.getName(),
                playerState.getHp(),
                playerState.getMaxHp(),
                playerState.getBaseDamage(),
                playerState.getLevel(),
                playerState.getKarma(),
                playerState.getBackpack());
        return player;
    }

    private static String normalizeCharacterId(String rawCharacterId) {
        if (rawCharacterId == null) {
            return null;
        }
        return rawCharacterId.trim().toLowerCase();
    }

    private static String resolveGameName(GameState state) {
        if (state.getGameName() != null && !state.getGameName().trim().isEmpty()) {
            return state.getGameName();
        }
        if (state.getPlayerName() != null && !state.getPlayerName().trim().isEmpty()) {
            return state.getPlayerName();
        }
        return "Partita caricata";
    }
}

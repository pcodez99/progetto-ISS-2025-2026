package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.factory.CharacterFactory;
import io.github.iss_2025_2026.factory.YamlCharacterFactory;
import io.github.iss_2025_2026.map.CheckpointDefinition;
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
        saveManual(model);
    }

    public static SaveResult saveManual(GameModel model) throws IOException {
        if (model == null || !model.hasGameStarted() || model.getPlayerOne() == null) {
            throw new IOException("Nessuna partita avviata da salvare.");
        }

        String saveFileName = SaveManager.toSaveFileName(model.getGameName());
        GameState state = toGameState(model);
        state.setSaveType(GameState.SaveType.MANUAL);
        SaveValidator.validateForSave(state);
        SaveManager.saveGame(state, saveFileName);
        model.setCurrentSaveFileName(saveFileName);
        model.setMessage("Partita salvata.");
        return SaveResult.success("Partita salvata.", saveFileName);
    }

    public static SaveResult saveAutoAtCheckpoint(GameModel model, CheckpointDefinition checkpoint) throws IOException {
        if (model == null || checkpoint == null || checkpoint.getId() == null || checkpoint.getId().trim().isEmpty()) {
            throw new IOException("Checkpoint non valido: salvataggio automatico annullato.");
        }
        if (!model.hasGameStarted() || model.getPlayerOne() == null) {
            throw new IOException("Nessuna partita avviata da salvare.");
        }

        int currentLevelId = model.getGameState().getCurrentLevelId();
        if (model.getGameState().isLastCheckpoint(checkpoint.getId(), currentLevelId)) {
            return SaveResult.skipped("Checkpoint gia salvato.", resolveCurrentSaveFileName(model));
        }

        String previousCheckpointId = model.getGameState().getLastCheckpointId();
        int previousCheckpointLevelId = model.getGameState().getLastCheckpointLevelId();
        String saveFileName = resolveCurrentSaveFileName(model);

        model.getGameState().setLastCheckpointId(checkpoint.getId());
        model.getGameState().setLastCheckpointLevelId(currentLevelId);
        try {
            GameState state = toGameState(model);
            state.setSaveType(GameState.SaveType.AUTO);
            SaveValidator.validateForSave(state);
            SaveManager.saveGame(state, saveFileName);
            model.setCurrentSaveFileName(saveFileName);
            model.setMessage("Checkpoint raggiunto - partita salvata.");
            return SaveResult.success("Checkpoint raggiunto - partita salvata.", saveFileName);
        } catch (IOException exception) {
            model.getGameState().setLastCheckpointId(previousCheckpointId);
            model.getGameState().setLastCheckpointLevelId(previousCheckpointLevelId);
            throw exception;
        }
    }

    private static String resolveCurrentSaveFileName(GameModel model) {
        String currentSaveFileName = model.getCurrentSaveFileName();
        if (currentSaveFileName != null && !currentSaveFileName.trim().isEmpty()) {
            return SaveManager.toSaveFileName(currentSaveFileName);
        }
        return SaveManager.toSaveFileName(model.getGameName());
    }

    public static void loadGameIntoModel(GameModel model, String fileName) throws IOException {
        GameState state = SaveManager.loadGame(fileName);
        SaveValidator.validateForLoad(state);
        loadGameIntoModel(model, state);
        model.setCurrentSaveFileName(SaveManager.toSaveFileName(fileName));
        model.setMessage("Salvataggio caricato.");
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
        state.setLastCheckpointId(model.getGameState().getLastCheckpointId());
        state.setLastCheckpointLevelId(model.getGameState().getLastCheckpointLevelId());
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
            Player legacyPlayer = createPlayerFromSave(factory, normalizeCharacterId(state.getCharacterType()));
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
        model.getGameState().setLastCheckpointId(state.getLastCheckpointId());
        model.getGameState().setLastCheckpointLevelId(state.getLastCheckpointLevelId());
    }

    private static PlayerSaveState toPlayerSaveState(Player player) {
        if (player == null) {
            return null;
        }

        Backpack backpack = player.getBackpack();
        PlayerSaveState state = new PlayerSaveState(
                player.getCharacterId(),
                player.getName(),
                player.getHp(),
                player.getMaxHp(),
                player.getBaseDamage(),
                player.getLevel(),
                player.getKarma(),
                backpack);
        state.setX(player.getX());
        state.setY(player.getY());
        state.setDirection(player.getDirection());
        state.setState(player.getState());
        return state;
    }

    private static Player restorePlayer(CharacterFactory factory, PlayerSaveState playerState) throws IOException {
        String characterId = normalizeCharacterId(playerState.getCharacterId());
        if (characterId == null || characterId.isEmpty()) {
            throw new IOException("Salvataggio corrotto: characterId mancante.");
        }

        Player player = createPlayerFromSave(factory, characterId);
        player.restoreState(
                playerState.getName(),
                playerState.getHp(),
                playerState.getMaxHp(),
                playerState.getBaseDamage(),
                playerState.getLevel(),
                playerState.getKarma(),
                playerState.getBackpack());
        player.setX(playerState.getX());
        player.setY(playerState.getY());
        player.setDirection(playerState.getDirection());
        player.setState(playerState.getState());
        return player;
    }

    private static Player createPlayerFromSave(CharacterFactory factory, String characterId) throws IOException {
        try {
            Player player = factory.createPlayer(characterId);
            if (player == null) {
                throw new IOException("Salvataggio corrotto: characterId non valido (" + characterId + ").");
            }
            return player;
        } catch (RuntimeException exception) {
            throw new IOException("Salvataggio corrotto: characterId non valido (" + characterId + ").", exception);
        }
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

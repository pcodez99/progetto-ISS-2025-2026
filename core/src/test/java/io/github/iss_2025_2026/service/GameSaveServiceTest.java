package io.github.iss_2025_2026.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import io.github.iss_2025_2026.map.CheckpointDefinition;
import io.github.iss_2025_2026.model.Backpack;
import io.github.iss_2025_2026.model.AbilitySlot;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.NewGameConfigModel;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.PlayerSaveState;
import io.github.iss_2025_2026.persistence.SaveManager;
import java.io.File;
import java.io.IOException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class GameSaveServiceTest {
    private static final String SAVE_DIRECTORY = "saves";
    private static final String TEST_GAME_NAME = "service_test_save";
    private static final String LOADED_SAVE_NAME = "service_loaded_slot";

    @BeforeAll
    static void initGdx() {
        new HeadlessApplication(new ApplicationListener() {
            @Override public void create() {}
            @Override public void resize(int width, int height) {}
            @Override public void render() {}
            @Override public void pause() {}
            @Override public void resume() {}
            @Override public void dispose() {}
        });
    }

    @AfterEach
    void tearDown() {
        deleteIfExists(SaveManager.toSaveFileName(TEST_GAME_NAME));
        deleteIfExists(SaveManager.toAutoSaveFileName(TEST_GAME_NAME));
        deleteIfExists(SaveManager.toSaveFileName(LOADED_SAVE_NAME));
        deleteIfExists(SaveManager.toAutoSaveFileName(LOADED_SAVE_NAME));
    }

    @Test
    void toGameStateCapturesProgressAndPlayerRuntimeState() {
        GameModel model = runningModel();
        model.getGameState().addCompletedLevelId(1);
        model.getGameState().setLastCheckpointId("campagna_cp_1");
        model.getGameState().setLastCheckpointLevelId(1);

        GameState state = GameSaveService.toGameState(model);

        assertEquals(1, state.getCurrentLevelId());
        assertEquals("campagna_cp_1", state.getLastCheckpointId());
        assertEquals(1, state.getCompletedLevelIds().size());
        assertEquals(321f, state.getPlayerOne().getX(), 0.001f);
        assertEquals(654f, state.getPlayerOne().getY(), 0.001f);
        assertEquals(Direction.RIGHT, state.getPlayerOne().getDirection());
        assertTrue(state.getPlayerOne().getEvolutionState().isAbilitySlotUnlocked(AbilitySlot.BASE));
    }

    @Test
    void autosaveWritesOnlyWhenCheckpointChanges() throws IOException {
        GameModel model = runningModel();
        CheckpointDefinition checkpoint = new CheckpointDefinition(
                "campagna_cp_1", "checkpoint", "checkpoint", 96f, true);

        SaveResult firstSave = GameSaveService.saveAutoAtCheckpoint(model, checkpoint);
        SaveResult secondSave = GameSaveService.saveAutoAtCheckpoint(model, checkpoint);
        GameState savedState = SaveManager.loadGame(TEST_GAME_NAME);

        assertTrue(firstSave.isSuccess());
        assertEquals("Checkpoint raggiunto - partita salvata.", firstSave.getMessage());
        assertEquals(SaveResult.Status.SKIPPED, secondSave.getStatus());
        assertEquals(SaveManager.toSaveFileName(TEST_GAME_NAME), firstSave.getFileName());
        assertEquals(SaveManager.toSaveFileName(TEST_GAME_NAME), secondSave.getFileName());
        assertEquals(GameState.SaveType.AUTO, savedState.getSaveType());
        assertEquals("campagna_cp_1", savedState.getLastCheckpointId());
        assertEquals(0, countAutosaveFilesForTestGame());
    }

    @Test
    void autosaveOverwritesSameFileWhenCheckpointChanges() throws IOException {
        GameModel model = runningModel();
        model.setCurrentSaveFileName(SaveManager.toSaveFileName(TEST_GAME_NAME));
        CheckpointDefinition firstCheckpoint = new CheckpointDefinition(
                "campagna_cp_1", "checkpoint", "checkpoint", 96f, true);
        CheckpointDefinition secondCheckpoint = new CheckpointDefinition(
                "campagna_cp_2", "checkpoint", "checkpoint", 96f, true);

        SaveResult firstSave = GameSaveService.saveAutoAtCheckpoint(model, firstCheckpoint);
        model.getPlayerOne().takeDamage(30);
        SaveResult secondSave = GameSaveService.saveAutoAtCheckpoint(model, secondCheckpoint);
        GameState savedState = SaveManager.loadGame(TEST_GAME_NAME);

        assertEquals(firstSave.getFileName(), secondSave.getFileName());
        assertEquals(SaveManager.toSaveFileName(TEST_GAME_NAME), secondSave.getFileName());
        assertEquals(0, countAutosaveFilesForTestGame());
        assertEquals("campagna_cp_2", savedState.getLastCheckpointId());
        assertEquals(70, savedState.getPlayerOne().getHp());
    }

    @Test
    void autosaveOverwritesLoadedSaveFileInsteadOfCreatingAutosaveFile() throws IOException {
        GameModel original = runningModel();
        SaveManager.saveGame(GameSaveService.toGameState(original), LOADED_SAVE_NAME);

        GameModel loaded = new GameModel();
        GameSaveService.loadGameIntoModel(loaded, LOADED_SAVE_NAME);
        loaded.getPlayerOne().takeDamage(35);

        SaveResult result = GameSaveService.saveAutoAtCheckpoint(
                loaded,
                new CheckpointDefinition("campagna_cp_1", "checkpoint", "checkpoint", 96f, true));

        GameState overwrittenState = SaveManager.loadGame(LOADED_SAVE_NAME);
        assertEquals(SaveManager.toSaveFileName(LOADED_SAVE_NAME), result.getFileName());
        assertEquals(65, overwrittenState.getPlayerOne().getHp());
        assertEquals("campagna_cp_1", overwrittenState.getLastCheckpointId());
        assertEquals(0, countAutosaveFilesForLoadedSave());
    }

    @Test
    void manualSaveAndLoadRestorePlayerState() throws IOException {
        GameModel source = runningModel();
        source.getPlayerOne().takeDamage(25);
        source.getPlayerOne().getEvolutionState().addAltruism(30);
        source.getPlayerOne().unlockAbilitySlot(AbilitySlot.ALTRUISTIC);
        GameSaveService.saveManual(source);

        GameModel restored = new GameModel();
        GameSaveService.loadGameIntoModel(restored, SaveManager.toSaveFileName(TEST_GAME_NAME));

        assertEquals(TEST_GAME_NAME, restored.getGameName());
        assertEquals(75, restored.getPlayerOne().getHp());
        assertEquals(321f, restored.getPlayerOne().getX(), 0.001f);
        assertEquals(654f, restored.getPlayerOne().getY(), 0.001f);
        assertEquals(Direction.RIGHT, restored.getPlayerOne().getDirection());
        assertEquals(30, restored.getPlayerOne().getEvolutionState().getAltruismScore());
        assertTrue(restored.getPlayerOne().isAbilitySlotUnlocked(AbilitySlot.ALTRUISTIC));
    }

    @Test
    void validatorRejectsCorruptedPlayerData() {
        PlayerSaveState corruptedPlayer = new PlayerSaveState(
                "mamma", "Mamma", 200, 100, 10, 1, 0, new Backpack());
        GameState corruptedState = new GameState(
                "Corrotto",
                NewGameConfigModel.GameMode.SINGLE_PLAYER,
                corruptedPlayer,
                null,
                "2026-05-31T12:00:00Z");

        assertThrows(IOException.class, () -> SaveValidator.validateForLoad(corruptedState));
    }

    private static GameModel runningModel() {
        GameModel model = new GameModel();
        Player player = new Player("Mamma", 100, 10, null);
        player.setCharacterId("mamma");
        player.setX(321f);
        player.setY(654f);
        player.setDirection(Direction.RIGHT);
        model.startSinglePlayerGame(TEST_GAME_NAME, player);
        return model;
    }

    private void deleteIfExists(String fileName) {
        File file = new File(SAVE_DIRECTORY, fileName);
        if (file.exists()) {
            file.delete();
        }
    }

    private int countAutosaveFilesForTestGame() {
        File saveDirectory = new File(SAVE_DIRECTORY);
        File[] files = saveDirectory.listFiles((dir, name) ->
                name.startsWith(TEST_GAME_NAME + "_autosave")
                        && name.endsWith(".json"));
        return files != null ? files.length : 0;
    }

    private int countAutosaveFilesForLoadedSave() {
        File saveDirectory = new File(SAVE_DIRECTORY);
        File[] files = saveDirectory.listFiles((dir, name) ->
                name.startsWith(LOADED_SAVE_NAME + "_autosave")
                        && name.endsWith(".json"));
        return files != null ? files.length : 0;
    }
}

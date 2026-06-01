package io.github.iss_2025_2026.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import io.github.iss_2025_2026.map.CheckpointDefinition;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.model.combat.BattleModel;
import io.github.iss_2025_2026.persistence.SaveManager;
import java.io.File;
import java.io.IOException;
import java.util.Collections;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Test TDD per GameOverService.
 */
public class GameOverServiceTest {
    private static final String TEST_GAME_NAME = "game_over_test_save";
    private final GameOverService service = new GameOverService();

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
    }

    @Test
    public void testCanRestoreWhenSaveFileExists() throws IOException {
        GameModel model = runningModelWithSave();

        assertTrue(service.canRestoreFromLastSave(model));
    }

    @Test
    public void testCannotRestoreWhenNoSaveFile() {
        GameModel model = runningModelWithoutSave();

        assertFalse(service.canRestoreFromLastSave(model));
    }

    @Test
    public void testRestoreReloadsPlayerHpFromSave() throws IOException {
        GameModel model = runningModelWithSave();
        model.getPlayerOne().takeDamage(model.getPlayerOne().getHp());
        model.setActiveBattleModel(new BattleModel(model.getPlayerOne(), null, Collections.emptyList()));

        GameOverResult result = service.restoreFromLastSave(model);

        assertEquals(GameOverResult.Status.RESTORED, result.getStatus());
        assertEquals(100, model.getPlayerOne().getHp());
        assertNull(model.getActiveBattleModel());
    }

    @Test
    public void testRestoreSetsPhaseToPlaying() throws IOException {
        GameModel model = runningModelWithSave();
        model.getGameState().setPhase(GameState.Phase.GAME_OVER);

        service.restoreFromLastSave(model);

        assertEquals(GameState.Phase.PLAYING, model.getGameState().getPhase());
    }

    @Test
    public void testRestorePreservesCheckpointMetadata() throws IOException {
        GameModel model = runningModelWithSave();
        CheckpointDefinition checkpoint = new CheckpointDefinition(
                "campagna_cp_1", "checkpoint", "checkpoint", 96f, true);
        GameSaveService.saveAutoAtCheckpoint(model, checkpoint);

        model.getPlayerOne().takeDamage(50);
        service.restoreFromLastSave(model);

        assertEquals("campagna_cp_1", model.getGameState().getLastCheckpointId());
        assertEquals(1, model.getGameState().getLastCheckpointLevelId());
    }

    @Test
    public void testEnterGameOverClearsBattleAndSetsPhase() {
        GameModel model = runningModelWithoutSave();
        model.setActiveBattleModel(new BattleModel(model.getPlayerOne(), null, Collections.emptyList()));

        service.enterGameOver(model);

        assertEquals(GameState.Phase.GAME_OVER, model.getGameState().getPhase());
        assertNull(model.getActiveBattleModel());
    }

    @Test
    public void testNoSaveFallbackHealsPlayersAndSetsPlaying() {
        GameModel model = runningModelWithoutSave();
        model.getPlayerOne().takeDamage(model.getPlayerOne().getHp());
        model.getGameState().setPhase(GameState.Phase.GAME_OVER);

        GameOverResult result = service.restoreFromLastSave(model);

        assertEquals(GameOverResult.Status.NO_SAVE_FALLBACK, result.getStatus());
        assertEquals(100, model.getPlayerOne().getHp());
        assertEquals(GameState.Phase.PLAYING, model.getGameState().getPhase());
    }

    private GameModel runningModelWithSave() throws IOException {
        GameModel model = runningModelWithoutSave();
        GameSaveService.saveManual(model);
        return model;
    }

    private GameModel runningModelWithoutSave() {
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
        File file = new File("saves", fileName);
        if (file.exists()) {
            file.delete();
        }
    }
}

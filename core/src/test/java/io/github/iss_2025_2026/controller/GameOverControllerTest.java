package io.github.iss_2025_2026.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.service.GameOverResult;
import io.github.iss_2025_2026.service.GameOverService;
import org.junit.jupiter.api.Test;

/**
 * Test TDD per GameOverController.
 */
public class GameOverControllerTest {

    @Test
    public void testOnContinueWithValidSaveRestoresAndResumesLevel() {
        RecordingResumeAction resumeAction = new RecordingResumeAction();
        StubGameOverService stubService = new StubGameOverService(
                GameOverResult.restored("Ripristinato.", "test.json"));
        GameModel model = new GameModel();
        model.startSinglePlayerGame("Test", new Player("Hero", 100, 10, null));
        GameOverController controller = new GameOverController(model, stubService, resumeAction);

        GameOverResult result = controller.onContinue();

        assertTrue(stubService.restoreCalled);
        assertTrue(resumeAction.levelResumed);
        assertEquals(GameOverResult.Status.RESTORED, result.getStatus());
    }

    @Test
    public void testOnContinueWithoutSaveStillResumesLevel() {
        RecordingResumeAction resumeAction = new RecordingResumeAction();
        StubGameOverService stubService = new StubGameOverService(
                GameOverResult.noSaveFallback("Nessun salvataggio."));
        GameModel model = new GameModel();
        model.startSinglePlayerGame("Test", new Player("Hero", 100, 10, null));
        GameOverController controller = new GameOverController(model, stubService, resumeAction);

        GameOverResult result = controller.onContinue();

        assertTrue(stubService.restoreCalled);
        assertTrue(resumeAction.levelResumed);
        assertEquals(GameOverResult.Status.NO_SAVE_FALLBACK, result.getStatus());
    }

    @Test
    public void testOnContinueWithErrorDoesNotResumeLevel() {
        RecordingResumeAction resumeAction = new RecordingResumeAction();
        StubGameOverService stubService = new StubGameOverService(
                GameOverResult.error("Errore di caricamento."));
        GameModel model = new GameModel();
        model.startSinglePlayerGame("Test", new Player("Hero", 100, 10, null));
        GameOverController controller = new GameOverController(model, stubService, resumeAction);

        GameOverResult result = controller.onContinue();

        assertTrue(stubService.restoreCalled);
        assertFalse(resumeAction.levelResumed);
        assertEquals(GameOverResult.Status.ERROR, result.getStatus());
    }

    private static final class RecordingResumeAction implements GameOverController.LevelResumeAction {
        private boolean levelResumed;

        @Override
        public void resumeLevel() {
            levelResumed = true;
        }
    }

    private static final class StubGameOverService extends GameOverService {
        private final GameOverResult result;
        private boolean restoreCalled;

        private StubGameOverService(GameOverResult result) {
            this.result = result;
        }

        @Override
        public GameOverResult restoreFromLastSave(GameModel model) {
            restoreCalled = true;
            model.getGameState().setPhase(GameState.Phase.PLAYING);
            return result;
        }
    }
}

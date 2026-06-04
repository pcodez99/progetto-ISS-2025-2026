package io.github.iss_2025_2026.service;

import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.GameState;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.persistence.SaveManager;
import java.io.File;
import java.io.IOException;

/**
 * Gestisce il ripristino della partita dopo un Game Over.
 */
public class GameOverService {

    public void enterGameOver(GameModel model) {
        if (model == null) {
            return;
        }
        model.setActiveBattleModel(null);
        model.getGameState().setPhase(GameState.Phase.GAME_OVER);
    }

    public boolean canRestoreFromLastSave(GameModel model) {
        String saveFileName = resolveSaveFileName(model);
        return saveFileName != null && saveFileExists(saveFileName);
    }

    public GameOverResult restoreFromLastSave(GameModel model) {
        if (model == null || !model.hasGameStarted()) {
            return GameOverResult.error("Nessuna partita attiva da ripristinare.");
        }

        model.setActiveBattleModel(null);

        String saveFileName = resolveSaveFileName(model);
        if (saveFileName != null && saveFileExists(saveFileName)) {
            try {
                GameSaveService.loadGameIntoModel(model, saveFileName);
                model.getGameState().setPhase(GameState.Phase.PLAYING);
                model.setMessage("Game Over - ripristino dall'ultimo checkpoint.");
                return GameOverResult.restored(
                        "Partita ripristinata dall'ultimo salvataggio.",
                        saveFileName);
            } catch (IOException exception) {
                return GameOverResult.error("Impossibile caricare il salvataggio: " + exception.getMessage());
            }
        }

        applyNoSaveFallback(model);
        return GameOverResult.noSaveFallback(
                "Nessun checkpoint trovato. La squadra e stata rinforzata.");
    }

    public String resolveSaveFileName(GameModel model) {
        if (model == null) {
            return null;
        }
        String currentSaveFileName = model.getCurrentSaveFileName();
        if (currentSaveFileName != null && !currentSaveFileName.trim().isEmpty()) {
            return SaveManager.toSaveFileName(currentSaveFileName);
        }
        if (model.getGameName() != null && !model.getGameName().trim().isEmpty()) {
            return SaveManager.toSaveFileName(model.getGameName());
        }
        return null;
    }

    private void applyNoSaveFallback(GameModel model) {
        healPlayers(model);
        model.getGameState().setPhase(GameState.Phase.PLAYING);
        model.setMessage("Nessun checkpoint disponibile. La squadra e stata curata.");
    }

    private void healPlayers(GameModel model) {
        restorePlayerHp(model.getPlayerOne());
        restorePlayerHp(model.getPlayerTwo());
    }

    private void restorePlayerHp(Player player) {
        if (player != null) {
            player.setHp(player.getMaxHp());
        }
    }

    private boolean saveFileExists(String saveFileName) {
        File file = new File("saves", saveFileName);
        return file.exists();
    }
}

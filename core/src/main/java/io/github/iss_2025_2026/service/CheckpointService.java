package io.github.iss_2025_2026.service;

import com.badlogic.gdx.math.Vector2;
import io.github.iss_2025_2026.map.CheckpointDefinition;
import io.github.iss_2025_2026.map.LevelRuntime;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.Player;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Detects checkpoint reach events and delegates autosave orchestration to GameSaveService.
 */
public final class CheckpointService {
    private final GameModel model;
    private final LevelRuntime levelRuntime;
    private final Map<String, Vector2> checkpointPositions = new HashMap<>();
    private final float playerSize;
    private final float playerYOffset;

    public CheckpointService(GameModel model, LevelRuntime levelRuntime) {
        this.model = model;
        this.levelRuntime = levelRuntime;
        this.playerSize = GameProperties.getFloat(GameProperties.KEY_PLAYER_SIZE, 160f);
        this.playerYOffset = playerSize * 0.48f;
    }

    public SaveResult pollCheckpointReachedEvent() {
        if (model == null || levelRuntime == null || !model.hasGameStarted()) {
            return null;
        }

        for (CheckpointDefinition checkpoint : levelRuntime.getDefinition().getCheckpoints()) {
            if (checkpoint == null || !checkpoint.isAutosave()) {
                continue;
            }
            if (model.getGameState().isLastCheckpoint(checkpoint.getId(), model.getGameState().getCurrentLevelId())) {
                continue;
            }
            if (!isReached(checkpoint)) {
                continue;
            }

            try {
                return GameSaveService.saveAutoAtCheckpoint(model, checkpoint);
            } catch (IOException exception) {
                return SaveResult.error("Autosalvataggio fallito: " + exception.getMessage(), null);
            } catch (RuntimeException exception) {
                return SaveResult.error("Checkpoint non valido: " + exception.getMessage(), null);
            }
        }

        return null;
    }

    public SaveResult update() {
        return pollCheckpointReachedEvent();
    }

    private boolean isReached(CheckpointDefinition checkpoint) {
        Vector2 checkpointPosition = checkpointPosition(checkpoint);
        return isPlayerInside(model.getPlayerOne(), checkpointPosition, checkpoint.getRadius())
                || isPlayerInside(model.getPlayerTwo(), checkpointPosition, checkpoint.getRadius());
    }

    private Vector2 checkpointPosition(CheckpointDefinition checkpoint) {
        Vector2 position = checkpointPositions.get(checkpoint.getId());
        if (position == null) {
            position = levelRuntime.getLevel().checkpointWorldPosition(checkpoint);
            checkpointPositions.put(checkpoint.getId(), position);
        }
        return position;
    }

    private boolean isPlayerInside(Player player, Vector2 checkpointPosition, float radius) {
        if (player == null || checkpointPosition == null) {
            return false;
        }

        float playerFootX = player.getX() + playerSize / 2f;
        float playerFootY = player.getY() + playerYOffset;
        float dx = playerFootX - checkpointPosition.x;
        float dy = playerFootY - checkpointPosition.y;
        return dx * dx + dy * dy <= radius * radius;
    }
}

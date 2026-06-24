package io.github.iss_2025_2026.service;

import com.badlogic.gdx.math.Vector2;
import io.github.iss_2025_2026.map.CheckpointDefinition;
import io.github.iss_2025_2026.map.LevelRuntime;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.Player;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Detects checkpoint reach events and delegates autosave orchestration to GameSaveService.
 */
public final class CheckpointService {
    private final GameModel model;
    private final LevelRuntime levelRuntime;
    private final Map<String, List<Vector2>> checkpointPolylines = new HashMap<>();
    private final float playerInteractionOffsetX;
    private final float playerInteractionOffsetY;

    public CheckpointService(GameModel model, LevelRuntime levelRuntime) {
        this(model, levelRuntime, defaultPlayerSize() / 2f, defaultPlayerSize() * 0.48f);
    }

    public CheckpointService(GameModel model, LevelRuntime levelRuntime,
            float playerInteractionOffsetX, float playerInteractionOffsetY) {
        this.model = model;
        this.levelRuntime = levelRuntime;
        this.playerInteractionOffsetX = playerInteractionOffsetX;
        this.playerInteractionOffsetY = playerInteractionOffsetY;
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
        List<Vector2> checkpointPolyline = checkpointPolyline(checkpoint);
        return isPlayerNearPolyline(
                    model.getPlayerOne(), checkpointPolyline, checkpoint.getRadius(),
                    playerInteractionOffsetX, playerInteractionOffsetY)
                || isPlayerNearPolyline(
                    model.getPlayerTwo(), checkpointPolyline, checkpoint.getRadius(),
                    playerInteractionOffsetX, playerInteractionOffsetY);
    }

    private List<Vector2> checkpointPolyline(CheckpointDefinition checkpoint) {
        List<Vector2> vertices = checkpointPolylines.get(checkpoint.getId());
        if (vertices == null) {
            vertices = levelRuntime.getLevel().checkpointWorldPolyline(checkpoint);
            checkpointPolylines.put(checkpoint.getId(), vertices);
        }
        return vertices;
    }

    static boolean isPlayerNearPolyline(Player player, List<Vector2> vertices, float radius,
            float playerInteractionOffsetX, float playerInteractionOffsetY) {
        if (player == null || vertices == null || vertices.size() < 2 || radius <= 0f) {
            return false;
        }

        float playerCenterX = player.getX() + playerInteractionOffsetX;
        float playerCenterY = player.getY() + playerInteractionOffsetY;
        float radiusSquared = radius * radius;
        for (int index = 0; index < vertices.size() - 1; index++) {
            if (distanceSquaredToSegment(
                    playerCenterX, playerCenterY, vertices.get(index), vertices.get(index + 1))
                    <= radiusSquared) {
                return true;
            }
        }
        return false;
    }

    private static float distanceSquaredToSegment(float pointX, float pointY, Vector2 start, Vector2 end) {
        float segmentX = end.x - start.x;
        float segmentY = end.y - start.y;
        float lengthSquared = segmentX * segmentX + segmentY * segmentY;
        if (lengthSquared == 0f) {
            float dx = pointX - start.x;
            float dy = pointY - start.y;
            return dx * dx + dy * dy;
        }

        float projection = ((pointX - start.x) * segmentX + (pointY - start.y) * segmentY)
                / lengthSquared;
        float clampedProjection = Math.max(0f, Math.min(1f, projection));
        float closestX = start.x + clampedProjection * segmentX;
        float closestY = start.y + clampedProjection * segmentY;
        float dx = pointX - closestX;
        float dy = pointY - closestY;
        return dx * dx + dy * dy;
    }

    private static float defaultPlayerSize() {
        return GameProperties.getFloat(GameProperties.KEY_PLAYER_SIZE, 160f);
    }
}

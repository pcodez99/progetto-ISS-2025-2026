package io.github.iss_2025_2026.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.badlogic.gdx.math.Vector2;
import io.github.iss_2025_2026.model.Player;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CheckpointServiceTest {
    private static final float PLAYER_OFFSET_X = 80f;
    private static final float PLAYER_OFFSET_Y = 76.8f;

    @Test
    void checkpointUsesDistanceFromTheActualPolyline() {
        Player player = new Player("Mamma", 100, 10, null);
        List<Vector2> checkpoint = Arrays.asList(
                new Vector2(200f, 100f),
                new Vector2(200f, 400f));

        setPlayerCenter(player, 141f, 250f);
        assertTrue(isNear(player, checkpoint, 60f));

        setPlayerCenter(player, 259f, 250f);
        assertTrue(isNear(player, checkpoint, 60f));

        setPlayerCenter(player, 139f, 250f);
        assertFalse(isNear(player, checkpoint, 60f));

        setPlayerCenter(player, 200f, 1_000f);
        assertFalse(isNear(player, checkpoint, 60f));
    }

    private static boolean isNear(Player player, List<Vector2> checkpoint, float radius) {
        return CheckpointService.isPlayerNearPolyline(
                player, checkpoint, radius, PLAYER_OFFSET_X, PLAYER_OFFSET_Y);
    }

    private static void setPlayerCenter(Player player, float centerX, float centerY) {
        player.setX(centerX - PLAYER_OFFSET_X);
        player.setY(centerY - PLAYER_OFFSET_Y);
    }
}

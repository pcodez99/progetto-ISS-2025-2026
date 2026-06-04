package io.github.iss_2025_2026.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.github.iss_2025_2026.model.Player;
import org.junit.jupiter.api.Test;

class CheckpointServiceTest {
    @Test
    void checkpointIsReachedWhenPlayerCenterCrossesCheckpointXRegardlessOfY() {
        Player player = new Player("Mamma", 100, 10, null);
        float playerSize = 160f;
        float checkpointX = 200f;

        player.setX(39f);
        player.setY(-10_000f);
        assertFalse(CheckpointService.hasReachedCheckpointLine(player, checkpointX, playerSize));

        player.setX(120f);
        player.setY(10_000f);
        assertTrue(CheckpointService.hasReachedCheckpointLine(player, checkpointX, playerSize));

        player.setX(121f);
        player.setY(-50_000f);
        assertTrue(CheckpointService.hasReachedCheckpointLine(player, checkpointX, playerSize));
    }
}

package io.github.iss_2025_2026.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.Player;
import org.junit.jupiter.api.Test;

public class PlayerControllerTest {
    private final PlayerController controller = new PlayerController();

    @Test
    public void pressingRightMovesOnlyHorizontally() {
        Player player = playerAt(100f, 40f);

        controller.handleMovement(1f, player, false, false, false, true);

        assertEquals(241.421f, player.getX(), 0.01f);
        assertEquals(-101.421f, player.getY(), 0.01f);
        assertEquals(Direction.RIGHT, player.getDirection());
        assertEquals(CharacterState.WALKING, player.getState());
    }

    @Test
    public void pressingDownMovesBelowCurrentPosition() {
        Player player = playerAt(100f, 40f);

        controller.handleMovement(1f, player, false, true, false, false);

        assertEquals(-41.421f, player.getX(), 0.01f);
        assertEquals(-101.421f, player.getY(), 0.01f);
        assertEquals(Direction.DOWN, player.getDirection());
        assertEquals(CharacterState.WALKING, player.getState());
    }

    @Test
    public void statesTransitionCorrectly() {
        Player player = playerAt(100f, 40f);

        // Initially IDLE
        assertEquals(CharacterState.IDLE, player.getState());

        // Press right -> should become WALKING
        controller.handleMovement(1f, player, false, false, false, true, false);
        assertEquals(CharacterState.WALKING, player.getState());

        // No keys pressed -> should become IDLE
        controller.handleMovement(1f, player, false, false, false, false, false);
        assertEquals(CharacterState.IDLE, player.getState());

        // Trigger attack -> should become ATTACKING
        controller.handleMovement(1f, player, false, false, false, false, true);
        assertEquals(CharacterState.ATTACKING, player.getState());

        // During attack, movement is locked and coordinate doesn't change
        float startX = player.getX();
        controller.handleMovement(0.1f, player, false, false, false, true, false);
        assertEquals(CharacterState.ATTACKING, player.getState());
        assertEquals(startX, player.getX());

        // Ticking delta past attack duration (0.1s + 0.3s = 0.4s > 0.32s) -> returns to IDLE/moves
        controller.handleMovement(0.3f, player, false, false, false, false, false);
        assertEquals(CharacterState.IDLE, player.getState());
    }

    @Test
    public void playersCannotExceedMaxDistance() {
        Player p1 = playerAt(0f, 0f);
        Player p2 = playerAt(300f, 0f); // separated by 300 units

        // Under MAX_DISTANCE (400), should not be clamped
        controller.clampPlayerDistance(p1, p2, 400f);
        assertEquals(0f, p1.getX(), 0.01f);
        assertEquals(0f, p1.getY(), 0.01f);

        // Position changes such that distance is 500 (which is > 400)
        p1.setX(-200f); // distance becomes 300 - (-200) = 500
        controller.clampPlayerDistance(p1, p2, 400f);
        
        // P1 should be clamped relative to P2, exactly 400 units away.
        // Direction from P2 (300, 0) to P1 (-200, 0) is (-1, 0).
        // Clamped position = P2 + (-1, 0) * 400 = (300 - 400, 0) = (-100, 0).
        assertEquals(-100f, p1.getX(), 0.01f);
        assertEquals(0f, p1.getY(), 0.01f);
    }

    private static Player playerAt(float x, float y) {
        Player player = new Player("Hero", 100, 10, null);
        player.setX(x);
        player.setY(y);
        player.setSpeed(200f);
        return player;
    }
}

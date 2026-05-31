package io.github.iss_2025_2026.controller;

import static org.junit.jupiter.api.Assertions.*;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.backends.headless.HeadlessApplication;
import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.Player;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class GameControllerTest {

    @BeforeAll
    public static void init() {
        new HeadlessApplication(new ApplicationListener() {
            @Override public void create() {}
            @Override public void resize(int width, int height) {}
            @Override public void render() {}
            @Override public void pause() {}
            @Override public void resume() {}
            @Override public void dispose() {}
        });
    }

    @Test
    public void updateTicksModelTimer() {
        GameModel model = new GameModel();
        GameController controller = new GameController(model);
        
        Player playerOne = new Player("Hero", 100, 10, null);
        model.startSinglePlayerGame("Campagna", playerOne);

        float initialTimer = model.getTimer();
        controller.update(0.1f);
        
        assertEquals(initialTimer + 0.1f, model.getTimer(), 0.001f);
    }

    @Test
    public void updateClampsDistanceInMultiplayer() {
        GameModel model = new GameModel();
        GameController controller = new GameController(model);
        
        Player playerOne = new Player("Hero1", 100, 10, null);
        Player playerTwo = new Player("Hero2", 100, 10, null);
        
        // Spawn far apart
        playerOne.setX(0f);
        playerOne.setY(0f);
        playerTwo.setX(600f); // distance 600 > MAX_PLAYER_DISTANCE (400)
        playerTwo.setY(0f);

        model.startMultiplayerGame("Campagna", playerOne, playerTwo);

        // Update delta = 0f to trigger clamping without movement keys
        controller.update(0f);

        // Player 1 should be clamped relative to Player 2
        // Since we clamp Player 1 relative to Player 2, and then Player 2 relative to Player 1:
        // P1 starts at 0, P2 starts at 600.
        // Step 1: clamp P1 relative to P2 -> P1 is clamped to be 400 away from 600, so P1 = 200.
        // Step 2: clamp P2 relative to updated P1 (200) -> P2 is 400 away from 200, so P2 = 600.
        // Final positions: P1 = 200, P2 = 600. Distance is exactly 400.
        assertEquals(200f, playerOne.getX(), 0.01f);
        assertEquals(600f, playerTwo.getX(), 0.01f);
        
        double finalDist = Math.sqrt(Math.pow(playerOne.getX() - playerTwo.getX(), 2) + Math.pow(playerOne.getY() - playerTwo.getY(), 2));
        assertEquals(400f, finalDist, 0.01f);
    }

    @Test
    public void pressingRightMovesPlayerOnIsometricAxes() {
        GameController controller = new GameController(new GameModel());
        Player player = playerAt(100f, 40f);

        controller.handlePlayerMovement(1f, player, false, false, false, true);

        assertEquals(241.421f, player.getX(), 0.01f);
        assertEquals(-101.421f, player.getY(), 0.01f);
        assertEquals(Direction.RIGHT, player.getDirection());
        assertEquals(CharacterState.WALKING, player.getState());
    }

    @Test
    public void pressingDownMovesPlayerBelowCurrentPosition() {
        GameController controller = new GameController(new GameModel());
        Player player = playerAt(100f, 40f);

        controller.handlePlayerMovement(1f, player, false, true, false, false);

        assertEquals(-41.421f, player.getX(), 0.01f);
        assertEquals(-101.421f, player.getY(), 0.01f);
        assertEquals(Direction.DOWN, player.getDirection());
        assertEquals(CharacterState.WALKING, player.getState());
    }

    @Test
    public void playerStatesTransitionCorrectly() {
        GameController controller = new GameController(new GameModel());
        Player player = playerAt(100f, 40f);

        assertEquals(CharacterState.IDLE, player.getState());

        controller.handlePlayerMovement(1f, player, false, false, false, true, false);
        assertEquals(CharacterState.WALKING, player.getState());

        controller.handlePlayerMovement(1f, player, false, false, false, false, false);
        assertEquals(CharacterState.IDLE, player.getState());

        controller.handlePlayerMovement(1f, player, false, false, false, false, true);
        assertEquals(CharacterState.ATTACKING, player.getState());

        float startX = player.getX();
        controller.handlePlayerMovement(0.1f, player, false, false, false, true, false);
        assertEquals(CharacterState.ATTACKING, player.getState());
        assertEquals(startX, player.getX());

        controller.handlePlayerMovement(0.3f, player, false, false, false, false, false);
        assertEquals(CharacterState.IDLE, player.getState());
    }

    @Test
    public void playersCannotExceedConfiguredMaxDistance() {
        GameController controller = new GameController(new GameModel());
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(300f, 0f);

        controller.clampPlayerDistance(playerOne, playerTwo, 400f);
        assertEquals(0f, playerOne.getX(), 0.01f);
        assertEquals(0f, playerOne.getY(), 0.01f);

        playerOne.setX(-200f);
        controller.clampPlayerDistance(playerOne, playerTwo, 400f);

        assertEquals(-100f, playerOne.getX(), 0.01f);
        assertEquals(0f, playerOne.getY(), 0.01f);
    }

    private static Player playerAt(float x, float y) {
        Player player = new Player("Hero", 100, 10, null);
        player.setX(x);
        player.setY(y);
        player.setSpeed(200f);
        return player;
    }
}

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
    public void updateNormalizesInvalidMultiplayerDistanceSymmetrically() {
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

        // La correzione conserva il punto medio (300) e distribuisce equamente
        // l'eccesso: nessun giocatore viene trascinato dall'altro.
        assertEquals(100f, playerOne.getX(), 0.01f);
        assertEquals(500f, playerTwo.getX(), 0.01f);
        
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
    public void movementStatesTransitionCorrectlyWithoutExplorationAttack() {
        GameController controller = new GameController(new GameModel());
        Player player = playerAt(100f, 40f);

        assertEquals(CharacterState.IDLE, player.getState());

        controller.handlePlayerMovement(1f, player, false, false, false, true);
        assertEquals(CharacterState.WALKING, player.getState());

        controller.handlePlayerMovement(1f, player, false, false, false, false);
        assertEquals(CharacterState.IDLE, player.getState());

        player.setState(CharacterState.ATTACKING);
        controller.handlePlayerMovement(0.1f, player, false, false, false, false);
        assertEquals(CharacterState.IDLE, player.getState());
    }

    @Test
    public void playerTwoCannotDragPlayerOnePastMaximumDistance() {
        MultiplayerMovementConstraint constraint = new MultiplayerMovementConstraint();
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(400f, 0f);

        playerTwo.setX(450f);
        playerTwo.setState(CharacterState.WALKING);
        boolean rolledBack = constraint.rollbackIfExceeded(
                playerOne, playerTwo, 0f, 0f, 400f, 0f, 400f);

        assertTrue(rolledBack);
        assertEquals(0f, playerOne.getX(), 0.01f);
        assertEquals(400f, playerTwo.getX(), 0.01f);
        assertEquals(CharacterState.IDLE, playerTwo.getState());
    }

    @Test
    public void playerOneCannotDragPlayerTwoPastMaximumDistance() {
        MultiplayerMovementConstraint constraint = new MultiplayerMovementConstraint();
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(400f, 0f);

        playerOne.setX(-50f);
        boolean rolledBack = constraint.rollbackIfExceeded(
                playerOne, playerTwo, 0f, 0f, 400f, 0f, 400f);

        assertTrue(rolledBack);
        assertEquals(0f, playerOne.getX(), 0.01f);
        assertEquals(400f, playerTwo.getX(), 0.01f);
    }

    @Test
    public void playersCanMoveTogetherWithoutChangingTheirDistance() {
        MultiplayerMovementConstraint constraint = new MultiplayerMovementConstraint();
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(300f, 0f);

        playerOne.setX(50f);
        playerTwo.setX(350f);
        boolean rolledBack = constraint.rollbackIfExceeded(
                playerOne, playerTwo, 0f, 0f, 300f, 0f, 400f);

        assertFalse(rolledBack);
        assertEquals(50f, playerOne.getX(), 0.01f);
        assertEquals(350f, playerTwo.getX(), 0.01f);
    }

    @Test
    public void playersCanMoveCloserTogether() {
        MultiplayerMovementConstraint constraint = new MultiplayerMovementConstraint();
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(400f, 0f);

        playerOne.setX(50f);
        playerTwo.setX(350f);
        boolean rolledBack = constraint.rollbackIfExceeded(
                playerOne, playerTwo, 0f, 0f, 400f, 0f, 400f);

        assertFalse(rolledBack);
        assertEquals(300f, Math.abs(playerOne.getX() - playerTwo.getX()), 0.01f);
    }

    @Test
    public void playersBothStopWhenTheyMoveOutward() {
        MultiplayerMovementConstraint constraint = new MultiplayerMovementConstraint();
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(400f, 0f);

        playerOne.setX(-20f);
        playerTwo.setX(420f);
        boolean rolledBack = constraint.rollbackIfExceeded(
                playerOne, playerTwo, 0f, 0f, 400f, 0f, 400f);

        assertTrue(rolledBack);
        assertEquals(0f, playerOne.getX(), 0.01f);
        assertEquals(400f, playerTwo.getX(), 0.01f);
    }

    @Test
    public void diagonalMovementCannotBypassMaximumDistance() {
        MultiplayerMovementConstraint constraint = new MultiplayerMovementConstraint();
        float diagonalCoordinate = (float) (400f / Math.sqrt(2f));
        Player playerOne = playerAt(0f, 0f);
        Player playerTwo = playerAt(diagonalCoordinate, diagonalCoordinate);

        playerOne.setX(-10f);
        playerOne.setY(-10f);
        playerTwo.setX(diagonalCoordinate + 10f);
        playerTwo.setY(diagonalCoordinate + 10f);
        boolean rolledBack = constraint.rollbackIfExceeded(
                playerOne, playerTwo,
                0f, 0f, diagonalCoordinate, diagonalCoordinate, 400f);

        assertTrue(rolledBack);
        assertEquals(0f, playerOne.getX(), 0.01f);
        assertEquals(diagonalCoordinate, playerTwo.getX(), 0.01f);
    }

    private static Player playerAt(float x, float y) {
        Player player = new Player("Hero", 100, 10, null);
        player.setX(x);
        player.setY(y);
        player.setSpeed(200f);
        return player;
    }
}

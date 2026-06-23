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

    private static Player playerAt(float x, float y) {
        Player player = new Player("Hero", 100, 10, null);
        player.setX(x);
        player.setY(y);
        player.setSpeed(200f);
        return player;
    }
}

package io.github.iss_2025_2026.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.model.Player;
import io.github.iss_2025_2026.service.GameProperties;

/**
 * Game Controller (Parte del pattern MVC).
 * Intercetta l'input dell'utente e lo traduce in cambiamenti di stato nel Model.
 * Coordina l'aggiornamento della logica di gioco.
 */
public class GameController {
    public static final float MAX_PLAYER_DISTANCE = 400f;

    private final GameModel model;
    private final MultiplayerMovementConstraint multiplayerMovementConstraint;

    public GameController(GameModel model) {
        this.model = model;
        this.multiplayerMovementConstraint = new MultiplayerMovementConstraint();
    }

    public void update(float delta) {
        if (model.isMultiplayerGame() && model.getPlayerTwo() != null) {
            Player playerOne = model.getPlayerOne();
            Player playerTwo = model.getPlayerTwo();
            float maxDistance = GameProperties.getFloat(
                    GameProperties.KEY_MAX_PLAYER_DISTANCE, MAX_PLAYER_DISTANCE);

            multiplayerMovementConstraint.normalizeInitialDistance(playerOne, playerTwo, maxDistance);
            float previousPlayerOneX = playerOne.getX();
            float previousPlayerOneY = playerOne.getY();
            float previousPlayerTwoX = playerTwo.getX();
            float previousPlayerTwoY = playerTwo.getY();

            handlePlayerMovement(
                    delta,
                    playerOne,
                    Gdx.input.isKeyPressed(Input.Keys.W),
                    Gdx.input.isKeyPressed(Input.Keys.S),
                    Gdx.input.isKeyPressed(Input.Keys.A),
                    Gdx.input.isKeyPressed(Input.Keys.D));

            handlePlayerMovement(
                    delta,
                    playerTwo,
                    Gdx.input.isKeyPressed(Input.Keys.UP),
                    Gdx.input.isKeyPressed(Input.Keys.DOWN),
                    Gdx.input.isKeyPressed(Input.Keys.LEFT),
                    Gdx.input.isKeyPressed(Input.Keys.RIGHT));

            multiplayerMovementConstraint.rollbackIfExceeded(
                    playerOne, playerTwo,
                    previousPlayerOneX, previousPlayerOneY,
                    previousPlayerTwoX, previousPlayerTwoY,
                    maxDistance);
        } else {
            handlePlayerMovement(delta, model.getPlayerOne());
        }
        model.update(delta);
    }

    void handlePlayerMovement(float delta, Player player) {
        handlePlayerMovement(
                delta,
                player,
                Gdx.input.isKeyPressed(Input.Keys.W),
                Gdx.input.isKeyPressed(Input.Keys.S),
                Gdx.input.isKeyPressed(Input.Keys.A),
                Gdx.input.isKeyPressed(Input.Keys.D));
    }

    void handlePlayerMovement(
            float delta, Player player, boolean upPressed, boolean downPressed, boolean leftPressed,
            boolean rightPressed) {
        if (player == null) {
            return;
        }

        player.updateStateTime(delta);

        float isoX = 0;
        float isoY = 0;

        if (upPressed) {
            isoX += 1;
            isoY += 1;
            player.setDirection(Direction.UP);
        }
        if (downPressed) {
            isoX -= 1;
            isoY -= 1;
            player.setDirection(Direction.DOWN);
        }
        if (leftPressed) {
            isoX -= 1;
            isoY += 1;
            player.setDirection(Direction.LEFT);
        }
        if (rightPressed) {
            isoX += 1;
            isoY -= 1;
            player.setDirection(Direction.RIGHT);
        }

        if (isoX != 0 || isoY != 0) {
            float length = (float) Math.sqrt(isoX * isoX + isoY * isoY);
            isoX /= length;
            isoY /= length;

            player.setX(player.getX() + isoX * player.getSpeed() * delta);
            player.setY(player.getY() + isoY * player.getSpeed() * delta);
            player.setState(CharacterState.WALKING);
            return;
        }

        player.setState(CharacterState.IDLE);
    }

}

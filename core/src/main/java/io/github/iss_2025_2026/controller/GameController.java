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

    public GameController(GameModel model) {
        this.model = model;
    }

    public void update(float delta) {
        if (model.isMultiplayerGame() && model.getPlayerTwo() != null) {
            handlePlayerMovement(
                    delta,
                    model.getPlayerOne(),
                    Gdx.input.isKeyPressed(Input.Keys.W),
                    Gdx.input.isKeyPressed(Input.Keys.S),
                    Gdx.input.isKeyPressed(Input.Keys.A),
                    Gdx.input.isKeyPressed(Input.Keys.D),
                    Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.Z));

            handlePlayerMovement(
                    delta,
                    model.getPlayerTwo(),
                    Gdx.input.isKeyPressed(Input.Keys.UP),
                    Gdx.input.isKeyPressed(Input.Keys.DOWN),
                    Gdx.input.isKeyPressed(Input.Keys.LEFT),
                    Gdx.input.isKeyPressed(Input.Keys.RIGHT),
                    Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_0));

            // Applica il vincolo di distanza reciproca
            float maxDist = GameProperties.getFloat(GameProperties.KEY_MAX_PLAYER_DISTANCE, MAX_PLAYER_DISTANCE);
            clampPlayerDistance(model.getPlayerOne(), model.getPlayerTwo(), maxDist);
            clampPlayerDistance(model.getPlayerTwo(), model.getPlayerOne(), maxDist);
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
                Gdx.input.isKeyPressed(Input.Keys.D),
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.Z));
    }

    void handlePlayerMovement(
            float delta, Player player, boolean upPressed, boolean downPressed, boolean leftPressed,
            boolean rightPressed) {
        handlePlayerMovement(delta, player, upPressed, downPressed, leftPressed, rightPressed, false);
    }

    void handlePlayerMovement(
            float delta, Player player, boolean upPressed, boolean downPressed, boolean leftPressed,
            boolean rightPressed, boolean attackPressed) {
        if (player == null) {
            return;
        }

        player.updateStateTime(delta);

        if (player.getState() == CharacterState.ATTACKING) {
            if (player.getStateTime() >= player.getAttackDuration()) {
                player.setState(CharacterState.IDLE);
            } else {
                return;
            }
        }

        if (attackPressed) {
            player.setState(CharacterState.ATTACKING);
            return;
        }

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

    void clampPlayerDistance(Player firstPlayer, Player secondPlayer, float maxDistance) {
        if (firstPlayer == null || secondPlayer == null) {
            return;
        }

        float dx = firstPlayer.getX() - secondPlayer.getX();
        float dy = firstPlayer.getY() - secondPlayer.getY();
        float distance = (float) Math.sqrt(dx * dx + dy * dy);

        if (distance > maxDistance && distance > 0f) {
            float directionX = dx / distance;
            float directionY = dy / distance;

            firstPlayer.setX(secondPlayer.getX() + directionX * maxDistance);
            firstPlayer.setY(secondPlayer.getY() + directionY * maxDistance);
        }
    }
}

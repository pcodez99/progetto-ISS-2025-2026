package io.github.iss_2025_2026.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.Player;

/**
 * Controller dedicato alla gestione dell'input del Player (Movimento, etc).
 */
public class PlayerController {

    public void handleMovement(float delta, Player player) {
        handleMovement(
                delta,
                player,
                Gdx.input.isKeyPressed(Input.Keys.W),
                Gdx.input.isKeyPressed(Input.Keys.S),
                Gdx.input.isKeyPressed(Input.Keys.A),
                Gdx.input.isKeyPressed(Input.Keys.D),
                Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.Z));
    }

    public void handleMovement(
            float delta, Player player, boolean upPressed, boolean downPressed, boolean leftPressed, boolean rightPressed) {
        handleMovement(delta, player, upPressed, downPressed, leftPressed, rightPressed, false);
    }

    public void handleMovement(
            float delta, Player player, boolean upPressed, boolean downPressed, boolean leftPressed, boolean rightPressed, boolean attackPressed) {
        if (player == null) return;

        player.updateStateTime(delta);

        // State Machine transitions
        if (player.getState() == CharacterState.ATTACKING) {
            if (player.getStateTime() >= player.getAttackDuration()) {
                player.setState(CharacterState.IDLE);
            } else {
                // Sta ancora attaccando, non compie azioni di movimento
                return;
            }
        }

        if (attackPressed) {
            player.setState(CharacterState.ATTACKING);
            return;
        }

        // Movimento isometrico: la mappa si sviluppa da sinistra a destra.
        // Ogni tasto contribuisce agli assi isometrici X e Y simultaneamente:
        //   W → avanza ↗  (isoX +1, isoY +1)
        //   S → indietro ↙ (isoX -1, isoY -1)
        //   A → laterale ↖ (isoX -1, isoY +1)
        //   D → laterale ↘ (isoX +1, isoY -1)
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
            // Normalizziamo per evitare velocità doppia in diagonale
            float length = (float) Math.sqrt(isoX * isoX + isoY * isoY);
            isoX /= length;
            isoY /= length;

            player.setX(player.getX() + isoX * player.getSpeed() * delta);
            player.setY(player.getY() + isoY * player.getSpeed() * delta);
            player.setState(CharacterState.WALKING);
        } else {
            player.setState(CharacterState.IDLE);
        }
    }
}

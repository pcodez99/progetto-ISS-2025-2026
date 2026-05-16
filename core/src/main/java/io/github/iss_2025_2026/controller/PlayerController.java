package io.github.iss_2025_2026.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.iss_2025_2026.model.Direction;
import io.github.iss_2025_2026.model.Player;

/**
 * Controller dedicato alla gestione dell'input del Player (Movimento, etc).
 */
public class PlayerController {

    public void handleMovement(float delta, Player player) {
        if (player == null) return;

        float moveX = 0;
        float moveY = 0;

        if (Gdx.input.isKeyPressed(Input.Keys.W)) {
            moveY += 1;
            player.setDirection(Direction.UP);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.S)) {
            moveY -= 1;
            player.setDirection(Direction.DOWN);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.A)) {
            moveX -= 1;
            player.setDirection(Direction.LEFT);
        }
        if (Gdx.input.isKeyPressed(Input.Keys.D)) {
            moveX += 1;
            player.setDirection(Direction.RIGHT);
        }

        if (moveX != 0 || moveY != 0) {
            // Normalizziamo il vettore di movimento per evitare velocità doppie in diagonale
            float length = (float) Math.sqrt(moveX * moveX + moveY * moveY);
            moveX /= length;
            moveY /= length;

            player.setX(player.getX() + moveX * player.getSpeed() * delta);
            player.setY(player.getY() + moveY * player.getSpeed() * delta);
        }
    }
}

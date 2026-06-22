package io.github.iss_2025_2026.controller;

import io.github.iss_2025_2026.model.CharacterState;
import io.github.iss_2025_2026.model.Player;

/** Applica in modo simmetrico il limite di distanza tra i due giocatori. */
final class MultiplayerMovementConstraint {
    private static final float EPSILON = 0.001f;

    void normalizeInitialDistance(Player firstPlayer, Player secondPlayer, float maxDistance) {
        if (!isValid(firstPlayer, secondPlayer, maxDistance)) {
            return;
        }

        float dx = firstPlayer.getX() - secondPlayer.getX();
        float dy = firstPlayer.getY() - secondPlayer.getY();
        float distance = length(dx, dy);
        if (distance <= maxDistance || distance <= EPSILON) {
            return;
        }

        float midpointX = (firstPlayer.getX() + secondPlayer.getX()) * 0.5f;
        float midpointY = (firstPlayer.getY() + secondPlayer.getY()) * 0.5f;
        float halfDistance = maxDistance * 0.5f;
        float directionX = dx / distance;
        float directionY = dy / distance;

        firstPlayer.setX(midpointX + directionX * halfDistance);
        firstPlayer.setY(midpointY + directionY * halfDistance);
        secondPlayer.setX(midpointX - directionX * halfDistance);
        secondPlayer.setY(midpointY - directionY * halfDistance);
    }

    boolean rollbackIfExceeded(Player firstPlayer, Player secondPlayer,
            float previousFirstX, float previousFirstY,
            float previousSecondX, float previousSecondY,
            float maxDistance) {
        if (!isValid(firstPlayer, secondPlayer, maxDistance)) {
            return false;
        }

        float dx = firstPlayer.getX() - secondPlayer.getX();
        float dy = firstPlayer.getY() - secondPlayer.getY();
        if (length(dx, dy) <= maxDistance + EPSILON) {
            return false;
        }

        firstPlayer.setX(previousFirstX);
        firstPlayer.setY(previousFirstY);
        secondPlayer.setX(previousSecondX);
        secondPlayer.setY(previousSecondY);
        stopWalking(firstPlayer);
        stopWalking(secondPlayer);
        return true;
    }

    private boolean isValid(Player firstPlayer, Player secondPlayer, float maxDistance) {
        return firstPlayer != null && secondPlayer != null && maxDistance > 0f;
    }

    private float length(float x, float y) {
        return (float) Math.sqrt(x * x + y * y);
    }

    private void stopWalking(Player player) {
        if (player.getState() == CharacterState.WALKING) {
            player.setState(CharacterState.IDLE);
        }
    }
}

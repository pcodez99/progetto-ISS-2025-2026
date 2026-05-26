package io.github.iss_2025_2026.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.iss_2025_2026.model.GameModel;
import io.github.iss_2025_2026.service.GameProperties;

/**
 * Game Controller (Parte del pattern MVC).
 * Intercetta l'input dell'utente e lo traduce in cambiamenti di stato nel Model.
 * Coordina l'aggiornamento della logica di gioco.
 */
public class GameController {
    private final GameModel model;
    private final PlayerController playerController;

    public GameController(GameModel model) {
        this.model = model;
        this.playerController = new PlayerController();
    }

    public void handleInput() {
        if (Gdx.input.isKeyJustPressed(Input.Keys.SPACE)) {
            model.setMessage("Space pressed! Model updated by Controller.");
        }

        if (Gdx.input.justTouched()) {
            model.setMessage("Screen touched! Model updated by Controller.");
        }
    }

    public void update(float delta) {
        handleInput();
        if (model.isMultiplayerGame() && model.getPlayerTwo() != null) {
            playerController.handleMovement(
                    delta,
                    model.getPlayerOne(),
                    Gdx.input.isKeyPressed(Input.Keys.W),
                    Gdx.input.isKeyPressed(Input.Keys.S),
                    Gdx.input.isKeyPressed(Input.Keys.A),
                    Gdx.input.isKeyPressed(Input.Keys.D),
                    Gdx.input.isKeyJustPressed(Input.Keys.SPACE) || Gdx.input.isKeyJustPressed(Input.Keys.Z));

            playerController.handleMovement(
                    delta,
                    model.getPlayerTwo(),
                    Gdx.input.isKeyPressed(Input.Keys.UP),
                    Gdx.input.isKeyPressed(Input.Keys.DOWN),
                    Gdx.input.isKeyPressed(Input.Keys.LEFT),
                    Gdx.input.isKeyPressed(Input.Keys.RIGHT),
                    Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.isKeyJustPressed(Input.Keys.NUMPAD_0));

            // Applica il vincolo di distanza reciproca
            float maxDist = GameProperties.getFloat(GameProperties.KEY_MAX_PLAYER_DISTANCE, PlayerController.MAX_PLAYER_DISTANCE);
            playerController.clampPlayerDistance(model.getPlayerOne(), model.getPlayerTwo(), maxDist);
            playerController.clampPlayerDistance(model.getPlayerTwo(), model.getPlayerOne(), maxDist);
        } else {
            playerController.handleMovement(delta, model.getPlayerOne());
        }
        model.update(delta);
    }
}

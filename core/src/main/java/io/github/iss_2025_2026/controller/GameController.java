package io.github.iss_2025_2026.controller;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import io.github.iss_2025_2026.model.GameModel;

/**
 * Game Controller.
 * Handles user input and translates it into state changes in the Model.
 */
public class GameController {
    private final GameModel model;

    public GameController(GameModel model) {
        this.model = model;
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
        model.update(delta);
    }
}

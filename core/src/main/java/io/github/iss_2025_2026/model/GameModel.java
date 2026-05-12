package io.github.iss_2025_2026.model;

/**
 * Game Model.
 * Holds the state of the game and logic that doesn't depend on rendering or
 * input.
 */
public class GameModel {
    private String message;
    private float timer;

    public GameModel() {
        this.message = "Viddani VS Alieni - MVC Base Architecture";
        this.timer = 0;
    }

    public void update(float delta) {
        timer += delta;
    }

    public String getMessage() {
        return message;
    }

    public float getTimer() {
        return timer;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
